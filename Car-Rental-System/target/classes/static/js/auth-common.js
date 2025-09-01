// auth-common.js - Common JavaScript functions for authentication pages

/**
 * Shows a message with the specified type
 * @param {string} message - The message to display
 * @param {string} type - The type of message ('error' or 'success')
 * @param {string} containerId - The ID of the container to show the message in
 */
function showMessage(message, type, containerId = 'message-container') {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.textContent = message;
    container.className = `message ${type}`;
    container.style.display = 'block';

    // Scroll to message
    container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/**
 * Clears any displayed messages
 * @param {string} containerId - The ID of the message container
 */
function clearMessage(containerId = 'message-container') {
    const container = document.getElementById(containerId);
    if (container) {
        container.textContent = '';
        container.className = 'message';
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
 * Makes an API call to the backend
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

        const response = await fetch(endpoint, options);
        const result = await response.json();

        return {
            status: response.status,
            data: result
        };
    } catch (error) {
        console.error('API call failed:', error);
        return {
            status: 500,
            data: { message: 'Network error. Please try again.', success: false }
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
    // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
    return password.length >= 8;
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
        showMessage('Password must be at least 8 characters', 'error');
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
