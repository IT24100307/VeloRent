// forgot-password.js - Handles the forgot password form functionality

document.addEventListener('DOMContentLoaded', () => {
    const forgotPasswordForm = document.getElementById('forgot-password-form');
    
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', handleForgotPassword);
    }
});

/**
 * Handles the forgot password form submission
 * @param {Event} event - The form submission event
 */
async function handleForgotPassword(event) {
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
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    try {
        // Call the forgot password API
        const response = await callApi('/api/auth/forgot-password', 'POST', {
            email
        });
        
        // Handle the response
        if (response.data.success) {
            showMessage(response.data.message || 'Please use your authenticator app to reset your password.', 'success');
            
            // Store the email in session storage for the reset page
            sessionStorage.setItem('reset_email', email);
            
            // Redirect to reset password page after a short delay
            setTimeout(() => {
                window.location.href = '/reset-password';
            }, 2000);
        } else {
            showMessage(response.data.message || 'Failed to continue with 2FA. Please ensure 2FA is enabled for your account.', 'error');
        }
    } catch (error) {
        showMessage('An error occurred. Please try again later.', 'error');
        console.error('Forgot password error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
