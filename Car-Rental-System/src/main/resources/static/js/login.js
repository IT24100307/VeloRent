// login.js - Handles the login form functionality

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Check if we were redirected with a message
    const params = getQueryParams();
    if (params.message) {
        showMessage(decodeURIComponent(params.message), params.type || 'info');
    }
});

/**
 * Validates email format
 * @param {string} email - Email to validate
 * @returns {boolean} - True if email is valid
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Handles the login form submission
 * @param {Event} event - The form submission event
 */
async function handleLogin(event) {
    event.preventDefault();
    
    // Get form elements
    const form = event.target;
    const submitButton = form.querySelector('button[type="submit"]');
    
    // Clear previous messages
    clearMessage();
    
    // Get form data
    const email = form.email.value.trim();
    const password = form.password.value;
    
    // Enhanced client-side validation
    const validationErrors = [];
    
    // Email validation
    if (!email) {
        validationErrors.push('Email address is required.');
        form.email.focus();
    } else if (!isValidEmail(email)) {
        validationErrors.push('Please enter a valid email address.');
        form.email.focus();
    }
    
    // Password validation
    if (!password) {
        validationErrors.push('Password is required.');
        if (!email || isValidEmail(email)) form.password.focus();
    } else if (password.length < 6) {
        validationErrors.push('Password must be at least 6 characters long.');
        if (!email || isValidEmail(email)) form.password.focus();
    }
    
    // Display validation errors
    if (validationErrors.length > 0) {
        showMessage(validationErrors[0], 'error');
        return;
    }
    
    // Additional form validation
    if (!validateForm(form)) {
        return;
    }
    
    // Store email for later use (needed for 2FA)
    localStorage.setItem('userEmail', email);
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    try {
        // Call the login API
        const response = await callApi('/api/auth/login', 'POST', {
            email,
            password
        });
        
        // Handle the response
        if (response.data.success) {
            // Check if 2FA is required
            if (response.data.message === "Please complete 2FA verification." || 
                response.data.message.includes("2FA") || 
                response.data.token === null) {
                // Store email for the 2FA verification page
                sessionStorage.setItem('auth_email', email);
                
                // Show message briefly before redirecting
                showMessage('Two-factor authentication required. Redirecting...', 'info');
                
                // Redirect to 2FA verification page after a short delay
                setTimeout(() => {
                    window.location.href = '/verify-2fa';
                }, 1000);
            } else {
                // Regular login success
                showMessage(response.data.message || 'Login successful!', 'success');
                
                // Clear any existing user data to prevent conflicts between different accounts
                localStorage.clear();
                
                // Save the token and user role
                if (response.data.token) {
                    localStorage.setItem('token', response.data.token);
                    
                    if (response.data.role) {
                        localStorage.setItem('userRole', response.data.role);
                    }
                    
                    // Check for return URL parameter
                    const params = getQueryParams();
                    let redirectUrl = params.returnUrl ? decodeURIComponent(params.returnUrl) : '/dashboard';


                    if(response.data.redirect){
                        // If redirect url get from response
                        redirectUrl = response.data.redirect
                    }else if (!params.returnUrl ) {
                        // If no return URL specified, determine redirect URL based on user role
                        if (response.data.role) {
                            const role = response.data.role;
                            if (role.includes('FLEET_MANAGER')) {
                                redirectUrl = '/fleet-manager/dashboard';
                            } else if (role.includes('ADMIN') || role.includes('OWNER')) {
                                redirectUrl = '/admin/dashboard';
                            }
                        }
                    }

                    console.log('Role:', response.data.role);
                    console.log('Redirecting to:', redirectUrl);
                    
                    // Set user name in localStorage if available
                    if (response.data.userName) {
                        localStorage.setItem('userName', response.data.userName);
                    }

                    // Store user ID and email
                    if (response.data.userId) {
                        localStorage.setItem('userId', response.data.userId);
                    }

                    if (response.data.userEmail) {
                        localStorage.setItem('userEmail', response.data.userEmail);
                    }

                    // Store customer ID if available (for customer role)
                    if (response.data.customerId) {
                        localStorage.setItem('customerId', response.data.customerId);
                    }

                    // Redirect after a short delay
                    setTimeout(() => {
                        window.location.href = redirectUrl;
                    }, 1000);
                } else {
                    showMessage('Authentication successful but no token received. Please try again.', 'error');
                }
            }
        } else {
            // Display specific error message from server
            const errorMessage = response.data.message || 'Login failed. Please check your credentials.';
            showMessage(errorMessage, 'error');
            
            // Focus on appropriate field based on error type
            if (errorMessage.includes('email') || errorMessage.includes('account')) {
                form.email.focus();
            } else if (errorMessage.includes('password')) {
                form.password.focus();
            }
        }
    } catch (error) {
        // Handle network errors or other exceptions
        if (error.response && error.response.data && error.response.data.message) {
            showMessage(error.response.data.message, 'error');
        } else {
            showMessage('Unable to connect to server. Please check your internet connection and try again.', 'error');
        }
        console.error('Login error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
