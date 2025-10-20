// auth-common.js - Common JavaScript functions for authentication pages

/**
 * Shows a message with the specified type
 * Uses luxury themed alerts when available.
 * @param {string} message - The message to display
 * @param {'error'|'success'|'warning'|'info'} [type='info'] - Message type
 * @param {string} [containerId='message-container'] - Target container
 */
function showMessage(message, type = 'info', containerId = 'message-container') {
    const container = document.getElementById(containerId);
    if (!container) return;

    // If the login page injected styles, prefer that markup
    const useLuxury = container.classList.contains('message-container') || containerId === 'message-container';
    if (useLuxury) {
        let alertClass = 'alert-info-luxury';
        let icon = 'fas fa-info-circle';
        if (type === 'error') { alertClass = 'alert-error-luxury'; icon = 'fas fa-exclamation-triangle'; }
        else if (type === 'success') { alertClass = 'alert-success-luxury'; icon = 'fas fa-check-circle'; }
        else if (type === 'warning') { alertClass = 'alert-warning-luxury'; icon = 'fas fa-exclamation-circle'; }

        container.innerHTML = `
            <div class="alert-luxury ${alertClass}">
              <i class="${icon} mr-2"></i>${message}
            </div>`;
        container.style.display = 'block';

        if (type === 'success') {
            setTimeout(() => clearMessage(containerId), 5000);
        }
    } else {
        // Fallback basic rendering
        container.textContent = message;
        container.className = `message ${type}`;
        container.style.display = 'block';
    }

    container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/**
 * Clears any displayed messages
 * @param {string} containerId - The ID of the message container
 */
function clearMessage(containerId = 'message-container') {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '';
        // Keep existing classes (like message-container) and just hide
        container.style.display = 'none';
    }
}

/**
 * Sets button loading state
 * @param {HTMLButtonElement} button - The button element
 * @param {boolean} isLoading - Whether the button is in loading state
 */
function setButtonLoading(button, isLoading) {
    if (isLoading) {
        button.classList.add('loading');
        button.disabled = true;
        const originalText = button.getAttribute('data-text') || button.textContent;
        button.setAttribute('data-text', originalText);
        
    // Add spinner and "Please wait..." text
        button.innerHTML = '<span class="loading-spinner"></span> Please wait...';
    } else {
        button.classList.remove('loading');
        button.disabled = false;
        button.textContent = button.getAttribute('data-text') || button.textContent;
    }
}

/**
 * Validates an email address format
 * @param {string} email - The email address to validate
 * @returns {boolean} - Whether the email is valid
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Makes an API call to the backend with improved error handling and timeout
 * @param {string} endpoint - The API endpoint to call
 * @param {string} method - The HTTP method to use
 * @param {object} data - The data to send in the request body
 * @returns {Promise<object>} - The response from the API
 */
async function callApi(endpoint, method, data = null) {
    try {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
            },
            // Ensure cookies are included in requests for session-based auth
            credentials: 'include'
        };

        if (data) {
            options.body = JSON.stringify(data);
        }

        // Add JWT token to header if available (for authenticated endpoints)
        const token = localStorage.getItem('token');
        if (token) {
            console.log('Using auth token for API call to:', endpoint);
            options.headers.Authorization = `Bearer ${token}`;
        } else {
            console.log('No auth token available for API call to:', endpoint);
        }

        // Create an AbortController for timeout handling
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 15000); // 15 second timeout
        options.signal = controller.signal;

        console.log(`Making ${method} request to ${endpoint} with data:`, data);

        const response = await fetch(endpoint, options);

        // Clear the timeout since the request completed
        clearTimeout(timeoutId);

        // Attempt to parse JSON body even on non-2xx responses so we can surface backend messages
        let result = null;
        try {
            result = await response.json();
        } catch (e) {
            // Ignore parse errors
        }
        console.log(`Received response from ${endpoint} [${response.status}]`, result);

        return {
            status: response.status,
            data: result ?? { success: response.ok, message: response.statusText }
        };
    } catch (error) {
        console.error('API call failed:', error);

        // Specific error handling based on the type of error
        let errorMessage = 'Network error. Please try again.';
        let errorStatus = 500;

        if (error.name === 'AbortError') {
            errorMessage = 'Request timed out. Server might be overloaded. Please try again later.';
        } else if (error.message && error.message.includes('status: 401')) {
            errorMessage = 'Authentication failed. Please log in again.';
            errorStatus = 401;
        } else if (error.message && error.message.includes('status: 403')) {
            errorMessage = 'You do not have permission to perform this action.';
            errorStatus = 403;
        } else if (error.message && error.message.includes('status: 404')) {
            errorMessage = 'The requested resource was not found.';
            errorStatus = 404;
        }

        return {
            status: errorStatus,
            data: { message: errorMessage, success: false }
        };
    }
}

/**
 * Validates an email address format
 * @param {string} email - The email to validate
 * @returns {boolean} - Whether the email is valid
 */
function isValidEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

/**
 * Validates a password
 * @param {string} password - The password to validate
 * @returns {boolean} - Whether the password is valid
 */
function isValidPassword(password) {
    // Align with UI rule: at least 6 characters
    return password.length >= 6;
}

/**
 * Handles form validation for auth forms
 * @param {HTMLFormElement} form - The form to validate
 * @returns {boolean} - Whether the form is valid
 */
function validateForm(form) {
    let isValid = true;
    const inputs = form.querySelectorAll('input[required]');
    
    // Reset all validation styles
    inputs.forEach(input => {
        input.style.borderColor = '';
    });
    
    // Check each required field
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.style.borderColor = 'var(--error-color)';
            isValid = false;
        }
        
        // Email validation
        if (input.type === 'email' && !isValidEmail(input.value)) {
            input.style.borderColor = 'var(--error-color)';
            isValid = false;
        }
    });
    
    // Check password fields if present
    const password = form.querySelector('input[type="password"]');
    if (password && password.required && !isValidPassword(password.value)) {
        password.style.borderColor = 'var(--error-color)';
        showMessage('Password must be at least 6 characters', 'error');
        return false;
    }
    
    // Check password confirmation if present
    const passwordConfirm = form.querySelector('input[name="passwordConfirm"]');
    if (passwordConfirm && password && passwordConfirm.value !== password.value) {
        passwordConfirm.style.borderColor = 'var(--error-color)';
        showMessage('Passwords do not match', 'error');
        return false;
    }
    
    if (!isValid) {
        showMessage('Please fill in all required fields correctly', 'error');
    }
    
    return isValid;
}

/**
 * Check if user is already authenticated and redirect if needed
 */
function checkAuth() {
    const token = localStorage.getItem('token');
    
    // If we're on a login/register page but user is already logged in
    if (token && (window.location.pathname.includes('login') || window.location.pathname.includes('register'))) {
        window.location.href = '/dashboard'; // Redirect to dashboard
    }
}

/**
 * Extracts parameters from the URL query string
 * @returns {object} - The parsed query parameters
 */
function getQueryParams() {
    const params = {};
    const queryString = window.location.search.substring(1);
    const pairs = queryString.split('&');
    
    for (const pair of pairs) {
        if (pair) {
            const [key, value] = pair.split('=');
            params[decodeURIComponent(key)] = decodeURIComponent(value || '');
        }
    }
    
    return params;
}

// Run auth check when DOM is loaded
document.addEventListener('DOMContentLoaded', checkAuth);
