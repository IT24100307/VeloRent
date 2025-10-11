// reset-password.js - Handles the reset password form functionality

document.addEventListener('DOMContentLoaded', () => {
    const resetPasswordForm = document.getElementById('reset-password-form');
    
    if (resetPasswordForm) {
        // Fill in the email from session storage if available
        const storedEmail = sessionStorage.getItem('reset_email');
        if (storedEmail) {
            resetPasswordForm.email.value = storedEmail;
        } else {
            // If no email in session storage, redirect back to forgot password
            window.location.href = '/forgot-password';
            return;
        }
        
        resetPasswordForm.addEventListener('submit', handleResetPassword);
    }
}); 

/**
 * Handles the reset password form submission
 * @param {Event} event - The form submission event
 */
async function handleResetPassword(event) {
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
    const otp = form.otp.value.trim();
    const newPassword = form.newPassword.value;
    const confirmPassword = form.confirmPassword.value;
    
    // Validate authenticator code format
    if (!/^\d{6}$/.test(otp)) {
        if (window.showLuxuryMessage) {
            window.showLuxuryMessage('Authentication code must be exactly 6 digits', 'error');
        } else {
            showMessage('Authentication code must be 6 digits', 'error');
        }
        return;
    }
    
    // Confirm passwords match
    if (newPassword !== confirmPassword) {
        if (window.showLuxuryMessage) {
            window.showLuxuryMessage('Passwords do not match. Please check both fields.', 'error');
        } else {
            showMessage('Passwords do not match', 'error');
        }
        return;
    }
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    // Show loading message
    if (window.showLuxuryMessage) {
        window.showLuxuryMessage('Resetting your password...', 'info');
    }
    
    try {
        // Call the reset password API
        const response = await callApi('/api/auth/reset-password', 'POST', {
            email,
            otp,
            newPassword
        });
        
        // Handle the response
        if (response.data.success) {
            if (window.showLuxuryMessage) {
                window.showLuxuryMessage(response.data.message || 'Password has been reset successfully! Redirecting to login...', 'success');
            } else {
                showMessage(response.data.message || 'Password has been reset successfully.', 'success');
            }
            
            // Clear the session storage
            sessionStorage.removeItem('reset_email');
            
            // Redirect to login page after a short delay
            setTimeout(() => {
                window.location.href = '/login?message=' + encodeURIComponent('Your password has been reset successfully.') + '&type=success';
            }, 2000);
        } else {
            if (window.showLuxuryMessage) {
                window.showLuxuryMessage(response.data.message || 'Failed to reset password. Please verify your authentication code and try again.', 'error');
            } else {
                showMessage(response.data.message || 'Failed to reset password. Please try again.', 'error');
            }
        }
    } catch (error) {
        if (window.showLuxuryMessage) {
            window.showLuxuryMessage('Network error occurred. Please check your connection and try again.', 'error');
        } else {
            showMessage('An error occurred. Please try again later.', 'error');
        }
        console.error('Reset password error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
