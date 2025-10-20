/* 
 * VeloRent Luxury Theme - Page Enhancement Script
 * This script should be included in all HTML pages to ensure consistent luxury styling
 */

// Ensure luxury theme is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add luxury theme classes to body if not present
    if (!document.body.classList.contains('luxury-theme-loaded')) {
        document.body.classList.add('luxury-theme-loaded');
    }

    // Convert standard Bootstrap buttons to luxury buttons
    const standardButtons = document.querySelectorAll('.btn:not(.btn-luxury)');
    standardButtons.forEach(btn => {
        if (btn.classList.contains('btn-primary')) {
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-luxury', 'btn-luxury-gold');
        } else if (btn.classList.contains('btn-secondary')) {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-luxury', 'btn-luxury-silver');
        } else if (btn.classList.contains('btn-outline-primary')) {
            btn.classList.remove('btn-outline-primary');
            btn.classList.add('btn-luxury', 'btn-luxury-outline');
        }
    });

    // Convert standard alerts to luxury alerts
    const standardAlerts = document.querySelectorAll('.alert:not(.alert-luxury)');
    standardAlerts.forEach(alert => {
        alert.classList.add('alert-luxury');
        if (alert.classList.contains('alert-success')) {
            alert.classList.add('alert-success-luxury');
        } else if (alert.classList.contains('alert-danger')) {
            alert.classList.add('alert-error-luxury');
        } else if (alert.classList.contains('alert-warning')) {
            alert.classList.add('alert-warning-luxury');
        } else if (alert.classList.contains('alert-info')) {
            alert.classList.add('alert-info-luxury');
        }
    });

    // Convert standard cards to luxury cards
    const standardCards = document.querySelectorAll('.card:not(.card-luxury)');
    standardCards.forEach(card => {
        card.classList.add('card-luxury', 'hover-luxury');
    });

    // Convert standard forms to luxury forms
    const standardForms = document.querySelectorAll('form:not(.form-luxury)');
    standardForms.forEach(form => {
        if (!form.closest('.auth-container')) { // Skip auth forms that might have specific styling
            form.classList.add('form-luxury');
        }
    });

    // Convert standard form controls to luxury form controls
    const standardFormControls = document.querySelectorAll('.form-control:not(.form-control-luxury)');
    standardFormControls.forEach(control => {
        control.classList.add('form-control-luxury');
    });

    // Convert standard tables to luxury tables
    const standardTables = document.querySelectorAll('table:not(.table-luxury)');
    standardTables.forEach(table => {
        table.classList.add('table-luxury');
    });

    // Add animation classes to elements
    const elementsToAnimate = document.querySelectorAll('h1, h2, h3, .card, .btn, .alert');
    elementsToAnimate.forEach((element, index) => {
        if (!element.classList.contains('animate-fade-in-up')) {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                element.style.transition = 'all 0.6s ease';
                element.style.opacity = '1';
                element.style.transform = 'translateY(0)';
            }, 100 + (index * 50));
        }
    });

    // Add page loader if not present
    if (!document.querySelector('.page-loader')) {
        const loader = document.createElement('div');
        loader.className = 'page-loader';
        loader.innerHTML = `
            <div class="loader-content">
                <div class="loader-spinner"></div>
                <div class="loader-text">VeloRent</div>
            </div>
        `;
        document.body.insertBefore(loader, document.body.firstChild);
        
        // Auto-hide loader
        setTimeout(() => {
            loader.classList.add('fade-out');
            setTimeout(() => {
                loader.style.display = 'none';
            }, 600);
        }, 800);
    }

    // Enhance existing navbars
    const standardNavbars = document.querySelectorAll('.navbar:not(.navbar-luxury)');
    standardNavbars.forEach(navbar => {
        navbar.classList.add('navbar-luxury');
        
        // Update navbar brand
        const brand = navbar.querySelector('.navbar-brand');
        if (brand && !brand.classList.contains('navbar-brand-luxury')) {
            brand.classList.add('navbar-brand-luxury');
            if (!brand.querySelector('.fas')) {
                brand.innerHTML = '<i class="fas fa-crown mr-2"></i>' + brand.innerHTML;
            }
        }

        // Update nav links
        const navLinks = navbar.querySelectorAll('.nav-link:not(.nav-link-luxury)');
        navLinks.forEach(link => {
            link.classList.add('nav-link-luxury');
        });
    });

    // Add luxury styling to footers
    const footers = document.querySelectorAll('footer');
    footers.forEach(footer => {
        if (!footer.style.background) {
            footer.style.background = 'var(--luxury-black)';
            footer.style.borderTop = '1px solid rgba(212, 175, 55, 0.2)';
            footer.style.padding = '4rem 0 2rem';
            footer.style.color = 'var(--text-luxury-light)';
        }
    });

    console.log('VeloRent Luxury Theme - Page enhancements applied successfully');

    // Display flash message if present (set during login success)
    try {
        const raw = sessionStorage.getItem('flashMessage');
        if (raw) {
            const { message, type } = JSON.parse(raw);
            if (message) {
                if (window.showLuxuryNotification) {
                    window.showLuxuryNotification(message, type || 'info', 3500);
                } else {
                    alert(message);
                }
            }
            sessionStorage.removeItem('flashMessage');
        }
    } catch (_) { /* ignore */ }
});

// Global function to show luxury notifications
window.showLuxuryNotification = function(message, type = 'info', duration = 5000) {
    const notification = document.createElement('div');
    notification.className = `alert-luxury alert-${type}-luxury`;
    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        z-index: 10000;
        max-width: 400px;
        animation: luxuryFadeInRight 0.5s ease;
        box-shadow: var(--shadow-luxury);
    `;
    
    let icon = 'fas fa-info-circle';
    switch(type) {
        case 'success': icon = 'fas fa-check-circle'; break;
        case 'error': icon = 'fas fa-exclamation-triangle'; break;
        case 'warning': icon = 'fas fa-exclamation-circle'; break;
    }
    
    notification.innerHTML = `<i class="${icon} mr-2"></i>${message}`;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'luxuryFadeInRight 0.5s ease reverse';
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, duration);
};

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { showLuxuryNotification };
}