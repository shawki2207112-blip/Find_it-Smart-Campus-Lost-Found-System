/* Role-aware navbar injected into <div id="navbar"> on every page. */
(function renderNavbar() {
    const links = [];
    if (Auth.isLoggedIn()) {
        if (Auth.hasRole('ROLE_STUDENT')) {
            links.push(['/student/post-item.html', 'Post item'], ['/student/my-items.html', 'My items'],
                ['/student/search.html', 'Search'], ['/student/matches.html', 'Matches'],
                ['/student/claims.html', 'My claims']);
        }
        if (Auth.hasRole('ROLE_SECURITY_STAFF')) {
            links.push(['/security/claims.html', 'Claims'], ['/security/found-items.html', 'Found items']);
        }
        if (Auth.hasRole('ROLE_ADMIN')) {
            links.push(['/admin/users.html', 'Users'], ['/admin/stats.html', 'Statistics'],
                ['/admin/categories.html', 'Categories']);
        }
        links.push(['/notifications.html', 'Notifications']);
    } else {
        links.push(['/login.html', 'Login'], ['/register.html', 'Register']);
    }

    const items = links.map(([href, label]) =>
        '<li class="nav-item"><a class="nav-link" href="' + href + '">' + label + '</a></li>').join('');
    const logout = Auth.isLoggedIn()
        ? '<span class="navbar-text me-3">' + UI.esc(Auth.name()) + '</span>'
        + '<button class="btn btn-outline-light btn-sm" id="logoutBtn">Logout</button>'
        : '';

    document.getElementById('navbar').innerHTML =
        '<nav class="navbar navbar-expand-lg navbar-findit"><div class="container">'
        + '<a class="navbar-brand fw-bold" href="/index.html">FindIt</a>'
        + '<button class="navbar-toggler navbar-dark" type="button" data-bs-toggle="collapse" '
        + 'data-bs-target="#navMenu"><span class="navbar-toggler-icon"></span></button>'
        + '<div class="collapse navbar-collapse" id="navMenu"><ul class="navbar-nav me-auto">' + items + '</ul>'
        + logout + '</div></div></nav>';

    const button = document.getElementById('logoutBtn');
    if (button) {
        button.addEventListener('click', () => Auth.logout());
    }
})();
