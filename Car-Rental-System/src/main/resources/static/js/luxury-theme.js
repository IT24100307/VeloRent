/**
 * VeloRent Luxury Theme JavaScript
 * Handles animations, interactions, and theme enhancements
 */

class LuxuryTheme {
    constructor() {
        this.init();
    }

    init() {
        this.setupPageLoader();
        this.setupScrollEffects();
        this.setupAnimations();
        this.setupHoverEffects();
        this.setupFormEnhancements();
        this.setupNavbarEffects();
        this.setupParallaxEffects();
        this.setupSmoothTransitions();
    }

    // Page Loader Animation
    setupPageLoader() {
        window.addEventListener('load', () => {
            const loader = document.querySelector('.page-loader');
            if (loader) {
                setTimeout(() => {
                    loader.classList.add('fade-out');
                    setTimeout(() => {
                        loader.style.display = 'none';
                    }, 600);
                }, 300);
            }
        });
    }

    // Scroll Effects
    setupScrollEffects() {
        let ticking = false;

        const updateScrollEffects = () => {
            const scrollY = window.scrollY;
            
            // Navbar scroll effect
            const navbar = document.querySelector('.navbar-luxury');
            if (navbar) {
                if (scrollY > 50) {
                    navbar.classList.add('scrolled');
                } else {
                    navbar.classList.remove('scrolled');
                }
            }

            // Parallax background effect
            const body = document.body;
            if (body) {
                body.style.backgroundPosition = `0 ${scrollY * 0.5}px`;
            }

            ticking = false;
        };

        const requestScrollUpdate = () => {
            if (!ticking) {
                requestAnimationFrame(updateScrollEffects);
                ticking = true;
            }
        };

        window.addEventListener('scroll', requestScrollUpdate);
    }

    // Intersection Observer for Animations
    setupAnimations() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const element = entry.target;
                    
                    // Add animation classes based on data attributes
                    if (element.dataset.animation) {
                        element.classList.add(element.dataset.animation);
                    } else {
                        // Default animation
                        element.classList.add('animate-fade-in-up');
                    }

                    observer.unobserve(element);
                }
            });
        }, observerOptions);

        // Observe elements that should animate
        const animateElements = document.querySelectorAll(
            '.card-luxury, .form-luxury, h1, h2, h3, .btn-luxury, .alert-luxury'
        );
        
        animateElements.forEach((el, index) => {
            // Add initial state
            el.style.opacity = '0';
            el.style.transform = 'translateY(30px)';
            
            // Add delay based on index
            if (index > 0) {
                el.classList.add(`animate-delay-${Math.min(index, 5)}`);
            }
            
            observer.observe(el);
        });
    }

    // Enhanced Hover Effects
    setupHoverEffects() {
        // Button hover effects
        const buttons = document.querySelectorAll('.btn-luxury');
        buttons.forEach(button => {
            button.addEventListener('mouseenter', (e) => {
                this.createRippleEffect(e, button);
            });
        });

        // Card hover effects
        const cards = document.querySelectorAll('.card-luxury');
        cards.forEach(card => {
            card.addEventListener('mousemove', (e) => {
                this.createCardTiltEffect(e, card);
            });

            card.addEventListener('mouseleave', () => {
                card.style.transform = 'translateY(0) rotateX(0) rotateY(0)';
            });
        });
    }

    // Ripple Effect
    createRippleEffect(event, element) {
        const ripple = document.createElement('span');
        const rect = element.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;

        ripple.style.cssText = `
            position: absolute;
            width: ${size}px;
            height: ${size}px;
            left: ${x}px;
            top: ${y}px;
            background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
            border-radius: 50%;
            transform: scale(0);
            animation: ripple 0.6s ease-out;
            pointer-events: none;
            z-index: 1;
        `;

        element.style.position = 'relative';
        element.style.overflow = 'hidden';
        element.appendChild(ripple);

        setTimeout(() => {
            ripple.remove();
        }, 600);
    }

    // Card Tilt Effect
    createCardTiltEffect(event, card) {
        const rect = card.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;
        const mouseX = event.clientX - centerX;
        const mouseY = event.clientY - centerY;

        const rotateX = (mouseY / rect.height) * -10;
        const rotateY = (mouseX / rect.width) * 10;

        card.style.transform = `translateY(-8px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
    }

    // Form Enhancements
    setupFormEnhancements() {
        // Floating labels
        const inputs = document.querySelectorAll('.form-control-luxury');
        inputs.forEach(input => {
            // Add focus/blur effects
            input.addEventListener('focus', () => {
                input.parentElement.classList.add('focused');
            });

            input.addEventListener('blur', () => {
                if (!input.value) {
                    input.parentElement.classList.remove('focused');
                }
            });

            // Check if input has value on load
            if (input.value) {
                input.parentElement.classList.add('focused');
            }
        });

        // Form validation feedback
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', (e) => {
                this.validateForm(form, e);
            });
        });
    }

    // Form Validation
    validateForm(form, event) {
        const inputs = form.querySelectorAll('.form-control-luxury[required]');
        let isValid = true;

        inputs.forEach(input => {
            const errorElement = input.parentElement.querySelector('.error-message');
            
            if (!input.value.trim()) {
                isValid = false;
                input.classList.add('error');
                
                if (!errorElement) {
                    const error = document.createElement('div');
                    error.className = 'error-message';
                    error.style.cssText = `
                        color: #ef5350;
                        font-size: 0.875rem;
                        margin-top: 0.5rem;
                        animation: luxuryFadeInUp 0.3s ease;
                    `;
                    error.textContent = `${input.dataset.label || 'This field'} is required`;
                    input.parentElement.appendChild(error);
                }
            } else {
                input.classList.remove('error');
                if (errorElement) {
                    errorElement.remove();
                }
            }
        });

        if (!isValid) {
            event.preventDefault();
            this.showNotification('Please fill in all required fields', 'error');
        }
    }

    // Navbar Effects
    setupNavbarEffects() {
        const navLinks = document.querySelectorAll('.nav-link-luxury');
        const currentPath = window.location.pathname;

        navLinks.forEach(link => {
            // Highlight active page
            if (link.getAttribute('href') === currentPath) {
                link.classList.add('active');
                link.style.color = 'var(--luxury-gold)';
            }

            // Smooth hover effect
            link.addEventListener('mouseenter', () => {
                link.style.transform = 'translateY(-2px)';
            });

            link.addEventListener('mouseleave', () => {
                link.style.transform = 'translateY(0)';
            });
        });
    }

    // Parallax Effects
    setupParallaxEffects() {
        const parallaxElements = document.querySelectorAll('[data-parallax]');
        
        if (parallaxElements.length > 0) {
            window.addEventListener('scroll', () => {
                const scrolled = window.pageYOffset;
                
                parallaxElements.forEach(element => {
                    const speed = element.dataset.parallax || 0.5;
                    const yPos = -(scrolled * speed);
                    element.style.transform = `translateY(${yPos}px)`;
                });
            });
        }
    }

    // Smooth Transitions
    setupSmoothTransitions() {
        // Add transition class to elements after page load
        window.addEventListener('load', () => {
            document.body.classList.add('transitions-enabled');
        });

        // Smooth scroll for anchor links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    }

    // Notification System
    showNotification(message, type = 'info', duration = 5000) {
        const notification = document.createElement('div');
        notification.className = `alert-luxury alert-${type}-luxury notification`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            max-width: 400px;
            animation: luxuryFadeInRight 0.5s ease;
        `;
        notification.textContent = message;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.animation = 'luxuryFadeInRight 0.5s ease reverse';
            setTimeout(() => {
                notification.remove();
            }, 500);
        }, duration);
    }

    // Theme Toggle (for future use)
    toggleTheme() {
        document.body.classList.toggle('light-theme');
        localStorage.setItem('theme', 
            document.body.classList.contains('light-theme') ? 'light' : 'dark'
        );
    }

    // Initialize saved theme
    initSavedTheme() {
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'light') {
            document.body.classList.add('light-theme');
        }
    }

    // Utility function to add loading state to buttons
    static addLoadingState(button, text = 'Loading...') {
        button.disabled = true;
        button.dataset.originalText = button.textContent;
        button.innerHTML = `
            <span class="loading-spinner" style="
                display: inline-block;
                width: 16px;
                height: 16px;
                border: 2px solid transparent;
                border-top: 2px solid currentColor;
                border-radius: 50%;
                animation: luxurySpin 1s linear infinite;
                margin-right: 8px;
            "></span>
            ${text}
        `;
    }

    // Utility function to remove loading state from buttons
    static removeLoadingState(button) {
        button.disabled = false;
        button.textContent = button.dataset.originalText || button.textContent;
    }
}

// CSS for ripple animation
const rippleCSS = `
    @keyframes ripple {
        from {
            transform: scale(0);
            opacity: 1;
        }
        to {
            transform: scale(2);
            opacity: 0;
        }
    }
`;

// Add ripple CSS to head
const style = document.createElement('style');
style.textContent = rippleCSS;
document.head.appendChild(style);

// Initialize luxury theme when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.luxuryTheme = new LuxuryTheme();
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LuxuryTheme;
}