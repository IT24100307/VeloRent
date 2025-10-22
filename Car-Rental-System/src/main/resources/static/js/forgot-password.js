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
        if (response.data && response.data.success) {
            showMessage(response.data.message || 'Please use your authenticator app to reset your password.', 'success');

            // Store the email in session storage for the reset page
            sessionStorage.setItem('reset_email', email);

            // Redirect to reset password page after a short delay
            setTimeout(() => {
                window.location.href = '/reset-password';
            }, 2000);
            // Notify page listeners to clear loading states
            window.dispatchEvent(new CustomEvent('forgotPasswordResult', { detail: { success: true } }));
        } else {
            // Map backend messages to subtle, user-friendly hints
            const rawMsg = (response && response.data && response.data.message) ? response.data.message : '';
            let friendly = 'We couldn\'t continue with 2FA for this email.';
            let level = 'error';

            if (rawMsg.toLowerCase().includes('user not found')) {
                friendly = 'No account was found for this email. Please check the address and try again.';
                level = 'warning';
            } else if (rawMsg.toLowerCase().includes('two-factor authentication is not enabled')) {
                friendly = 'Two‑factor authentication isn\'t enabled for this account. Enable 2FA in Security Settings, then try again.';
                level = 'warning';
            }

            showMessage(friendly, level);
            // Notify page listeners to clear loading states
            window.dispatchEvent(new CustomEvent('forgotPasswordResult', { detail: { success: false } }));
        }
    } catch (error) {
        // Network/unknown errors
        showMessage('We couldn\'t process your request right now. Please try again in a moment.', 'error');
        window.dispatchEvent(new CustomEvent('forgotPasswordResult', { detail: { success: false } }));
        console.error('Forgot password error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
