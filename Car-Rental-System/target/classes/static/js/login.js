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
 * Handles the login form submission
 * @param {Event} event - The form submission event
 */
async function handleLogin(event) {
    event.preventDefault();
    
    // Get form elements
    const form = event.target;
    const submitButton = form.querySelector('button[type="submit"]');
    
    // Validate form
    if (!validateForm(form)) {
        return;
    }
    
    // Clear previous messages
    clearMessage();
    
    // Get form data
    const email = form.email.value.trim();
    const password = form.password.value;
    
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
                
                // Save the token and user role
                if (response.data.token) {
                    localStorage.setItem('token', response.data.token);
                    
                    if (response.data.role) {
                        localStorage.setItem('userRole', response.data.role);
                    }
                    
                    // Always redirect to /dashboard for all roles
                    const redirectUrl = '/dashboard';

                    console.log('Role:', response.data.role);
                    console.log('Redirecting to:', redirectUrl);
                    
                    // Redirect after a short delay
                    setTimeout(() => {
                        window.location.href = redirectUrl;
                    }, 1000);
                } else {
                    showMessage('Authentication successful but no token received. Please try again.', 'error');
                }
            }
        } else {
            showMessage(response.data.message || 'Invalid email or password', 'error');
        }
    } catch (error) {
        showMessage('An error occurred. Please try again later.', 'error');
        console.error('Login error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
