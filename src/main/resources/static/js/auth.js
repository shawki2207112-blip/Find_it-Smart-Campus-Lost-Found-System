/* Token storage and role helpers. The JWT is kept in localStorage under one key. */
const Auth = {
    KEY: 'findit_token',

    getToken() {
        return localStorage.getItem(this.KEY);
    },

    setToken(token) {
        localStorage.setItem(this.KEY, token);
    },

    clear() {
        localStorage.removeItem(this.KEY);
    },

    payload() {
        const token = this.getToken();
        if (!token) {
            return null;
        }
        try {
            const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            const json = decodeURIComponent(atob(base64).split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
            return JSON.parse(json);
        } catch (e) {
            return null;
        }
    },

    isLoggedIn() {
        const p = this.payload();
        return !!p && p.exp * 1000 > Date.now();
    },

    roles() {
        const p = this.payload();
        return p && p.roles ? p.roles : [];
    },

    hasRole(role) {
        return this.roles().includes(role);
    },

    name() {
        const p = this.payload();
        return p ? p.name : '';
    },

    homePage() {
        if (this.hasRole('ROLE_ADMIN')) return '/admin/stats.html';
        if (this.hasRole('ROLE_SECURITY_STAFF')) return '/security/claims.html';
        return '/student/search.html';
    },

    /** Redirects to the login page unless logged in (and, if given, holding the role). */
    require(role) {
        if (!this.isLoggedIn()) {
            this.clear();
            window.location.href = '/login.html';
            return false;
        }
        if (role && !this.hasRole(role)) {
            window.location.href = this.homePage();
            return false;
        }
        return true;
    },

    async logout() {
        try {
            await Api.post('/api/auth/logout');
        } catch (e) {
            /* the token may already be invalid; clearing it locally is enough */
        }
        this.clear();
        window.location.href = '/login.html';
    }
};
