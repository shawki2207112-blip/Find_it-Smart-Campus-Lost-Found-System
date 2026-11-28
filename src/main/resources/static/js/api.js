/* fetch wrapper: adds the Bearer token, redirects on 401 and turns ErrorResponse JSON into readable errors. */
class ApiError extends Error {
    constructor(status, message, fieldErrors) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors || null;
    }

    /** Message plus per-field details, ready to show to the user. */
    describe() {
        if (!this.fieldErrors) {
            return this.message;
        }
        const details = Object.entries(this.fieldErrors).map(([f, m]) => f + ': ' + m).join('; ');
        return this.message + ' (' + details + ')';
    }
}

const Api = {
    async request(method, path, body) {
        const headers = {'Content-Type': 'application/json'};
        const token = Auth.getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        const response = await fetch(path, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        });
        if (response.status === 401 && !path.startsWith('/api/auth/login') && !path.startsWith('/api/public')) {
            Auth.clear();
            window.location.href = '/login.html';
            throw new ApiError(401, 'Session expired, please log in again');
        }
        if (response.status === 204) {
            return null;
        }
        let data = null;
        try {
            data = await response.json();
        } catch (e) {
            data = null;
        }
        if (!response.ok) {
            const message = data && data.message ? data.message : 'Request failed (' + response.status + ')';
            throw new ApiError(response.status, message, data ? data.fieldErrors : null);
        }
        return data;
    },

    get(path) { return this.request('GET', path); },
    post(path, body) { return this.request('POST', path, body); },
    put(path, body) { return this.request('PUT', path, body); },
    del(path) { return this.request('DELETE', path); }
};

/* Small DOM helpers shared by all pages. */
const UI = {
    esc(value) {
        const div = document.createElement('div');
        div.textContent = value === null || value === undefined ? '' : String(value);
        return div.innerHTML;
    },

    qs(name) {
        return new URLSearchParams(window.location.search).get(name);
    },

    badge(status) {
        return '<span class="badge status-' + UI.esc(status) + '">' + UI.esc(status) + '</span>';
    },

    alert(message, type) {
        const box = document.getElementById('alerts');
        if (!box) return;
        box.innerHTML = '<div class="alert alert-' + (type || 'danger') + ' alert-dismissible">'
            + UI.esc(message) + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>';
        window.scrollTo(0, 0);
    },

    error(err) {
        UI.alert(err instanceof ApiError ? err.describe() : 'Unexpected error: ' + err.message, 'danger');
    },

    date(value) {
        return value ? new Date(value).toLocaleString() : '';
    },

    async fillCategories(selectId, withAll) {
        const select = document.getElementById(selectId);
        const categories = await Api.get('/api/categories');
        select.innerHTML = (withAll ? '<option value="">All categories</option>' : '')
            + categories.map(c => '<option value="' + (withAll ? UI.esc(c.name) : c.id) + '">'
                + UI.esc(c.name) + '</option>').join('');
    },

    itemLink(item) {
        return '<a href="/student/item-detail.html?id=' + item.id + '">' + UI.esc(item.title) + '</a>';
    }
};
