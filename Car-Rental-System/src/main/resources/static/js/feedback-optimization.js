/**
 * Feedback Page Enhancement Script
 * Optimizes loading and user interactions on the feedback page
 */

// Preload optimization
document.addEventListener('DOMContentLoaded', function() {
    // Remove any existing page loaders immediately
    const existingLoaders = document.querySelectorAll('.page-loader, .loading-spinner');
    existingLoaders.forEach(loader => {
        loader.style.display = 'none';
    });

    // Initialize page components
    initializeFeedbackPage();
});

function initializeFeedbackPage() {
    // Initialize star rating
    initializeStarRating();
    
    // Initialize form validation
    initializeFormValidation();
    
    // Initialize auto-dismiss alerts
    initializeAlertDismissal();
    
    // Initialize smooth animations
    initializeAnimations();
    
    // Optimize images loading
    optimizeImageLoading();
}

function initializeStarRating() {
    const starRatings = document.querySelectorAll('.star-rating');
    
    starRatings.forEach(rating => {
        const stars = rating.querySelectorAll('i');
        const input = rating.querySelector('input[name="rating"]');
        
        // Set default to 5 stars
        if (input && !input.value) {
            input.value = 5;
            stars.forEach(star => {
                star.classList.remove('far');
                star.classList.add('fas');
            });
        }
        
        stars.forEach(star => {
            star.addEventListener('click', function() {
                const ratingValue = parseInt(this.dataset.rating);
                input.value = ratingValue;
                
                stars.forEach((s, index) => {
                    if (index < ratingValue) {
                        s.classList.remove('far');
                        s.classList.add('fas');
                    } else {
                        s.classList.remove('fas');
                        s.classList.add('far');
                    }
                });
            });
            
            // Add hover effects
            star.addEventListener('mouseenter', function() {
                const hoverValue = parseInt(this.dataset.rating);
                stars.forEach((s, index) => {
                    if (index < hoverValue) {
                        s.style.color = '#d4af37';
                    } else {
                        s.style.color = '#6c757d';
                    }
                });
            });
        });
        
        // Reset on mouse leave
        rating.addEventListener('mouseleave', function() {
            const currentRating = parseInt(input.value) || 0;
            stars.forEach((s, index) => {
                if (index < currentRating) {
                    s.style.color = '#d4af37';
                } else {
                    s.style.color = '#6c757d';
                }
            });
        });
    });
}

function initializeFormValidation() {
    const feedbackForm = document.getElementById('feedbackForm');
    
    if (feedbackForm) {
        feedbackForm.addEventListener('submit', function(e) {
            const customerName = document.getElementById('customerName');
            const comments = document.getElementById('comments');
            const rating = document.getElementById('rating');
            
            // Validate customer name
            if (!customerName.value.trim()) {
                e.preventDefault();
                showValidationError(customerName, 'Please enter your name');
                return false;
            }
            
            // Validate comments
            if (!comments.value.trim()) {
                e.preventDefault();
                showValidationError(comments, 'Please enter your feedback');
                return false;
            }
            
            // Validate comments length
            if (comments.value.trim().length < 10) {
                e.preventDefault();
                showValidationError(comments, 'Feedback must be at least 10 characters long');
                return false;
            }
            
            // Validate rating
            if (!rating.value || rating.value < 1 || rating.value > 5) {
                e.preventDefault();
                alert('Please select a rating between 1 and 5 stars');
                return false;
            }
            
            // Show loading state
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Submitting...';
            submitBtn.disabled = true;
            
            // Clear any existing validation errors
            clearValidationErrors();
            
            return true;
        });
    }
}

function showValidationError(element, message) {
    // Remove existing error
    const existingError = element.parentNode.querySelector('.validation-error');
    if (existingError) {
        existingError.remove();
    }
    
    // Add error styling
    element.classList.add('is-invalid');
    
    // Create error message
    const errorDiv = document.createElement('div');
    errorDiv.className = 'validation-error text-danger mt-1';
    errorDiv.innerHTML = '<i class="fas fa-exclamation-triangle mr-1"></i>' + message;
    element.parentNode.appendChild(errorDiv);
    
    // Focus on the element
    element.focus();
    
    // Remove error after user starts typing
    element.addEventListener('input', function() {
        this.classList.remove('is-invalid');
        const error = this.parentNode.querySelector('.validation-error');
        if (error) {
            error.remove();
        }
    }, { once: true });
}

function clearValidationErrors() {
    const errorElements = document.querySelectorAll('.validation-error');
    const invalidElements = document.querySelectorAll('.is-invalid');
    
    errorElements.forEach(error => error.remove());
    invalidElements.forEach(element => element.classList.remove('is-invalid'));
}

function initializeAlertDismissal() {
    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert.parentNode) {
                alert.style.opacity = '0';
                setTimeout(() => {
                    if (alert.parentNode) {
                        alert.parentNode.removeChild(alert);
                    }
                }, 300);
            }
        }, 5000);
    });
}

function initializeAnimations() {
    // Add entrance animations to feedback items
    const feedbackItems = document.querySelectorAll('.feedback-item');
    feedbackItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            item.style.transition = 'all 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, index * 100);
    });
    
    // Add hover effects
    feedbackItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateX(10px)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateX(0)';
        });
    });
}

function optimizeImageLoading() {
    // Lazy load images if needed
    const images = document.querySelectorAll('img');
    
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                        observer.unobserve(img);
                    }
                }
            });
        });
        
        images.forEach(img => {
            if (img.dataset.src) {
                imageObserver.observe(img);
            }
        });
    }
}

// Smooth scrolling for anchor links
document.addEventListener('click', function(e) {
    if (e.target.matches('a[href^="#"]')) {
        e.preventDefault();
        const target = document.querySelector(e.target.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }
});

// Performance optimization: Debounce scroll events
let scrollTimeout;
window.addEventListener('scroll', function() {
    if (scrollTimeout) {
        clearTimeout(scrollTimeout);
    }
    
    scrollTimeout = setTimeout(function() {
        // Add any scroll-based functionality here
        const navbar = document.querySelector('.navbar-luxury');
        if (navbar) {
            if (window.scrollY > 100) {
                navbar.style.background = 'rgba(15, 15, 15, 0.98)';
            } else {
                navbar.style.background = 'rgba(15, 15, 15, 0.95)';
            }
        }
    }, 10);
});

// Prevent form double submission
document.addEventListener('submit', function(e) {
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    
    if (submitBtn && submitBtn.disabled) {
        e.preventDefault();
        return false;
    }
});

// Add keyboard navigation for star ratings
document.addEventListener('keydown', function(e) {
    if (e.target.closest('.star-rating')) {
        const rating = e.target.closest('.star-rating');
        const stars = rating.querySelectorAll('i');
        const input = rating.querySelector('input[name="rating"]');
        
        if (e.key >= '1' && e.key <= '5') {
            const ratingValue = parseInt(e.key);
            input.value = ratingValue;
            
            stars.forEach((star, index) => {
                if (index < ratingValue) {
                    star.classList.remove('far');
                    star.classList.add('fas');
                } else {
                    star.classList.remove('fas');
                    star.classList.add('far');
                }
            });
        }
    }
});

// Error boundary for JavaScript errors
window.addEventListener('error', function(e) {
    console.error('Feedback page error:', e.error);
    
    // Show user-friendly error message
    const errorAlert = document.createElement('div');
    errorAlert.className = 'alert alert-warning alert-dismissible fade show';
    errorAlert.innerHTML = `
        <i class="fas fa-exclamation-triangle mr-2"></i>
        <strong>Notice:</strong> Some features may not be working properly. Please refresh the page if you experience any issues.
        <button type="button" class="close" data-dismiss="alert">
            <span>&times;</span>
        </button>
    `;
    
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(errorAlert, container.firstChild);
    }
});

console.log('Feedback page optimization script loaded successfully');