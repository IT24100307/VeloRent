// verify-2fa.js - Handles the 2FA verification during login

document.addEventListener('DOMContentLoaded', () => {
    const verifyForm = document.getElementById('verify-2fa-form');
    
    if (verifyForm) {
        // Fill in the email from session storage if available
        const storedEmail = sessionStorage.getItem('auth_email');
        if (storedEmail) {
            verifyForm.email.value = storedEmail;
        } else {
            // If no email in session storage, redirect back to login
            window.location.href = '/login';
            return;
        }
        
        verifyForm.addEventListener('submit', handleVerify2FA);
    }
});

/**
 * Handles the 2FA verification form submission
 * @param {Event} event - The form submission event
 */
async function handleVerify2FA(event) {
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
    const code = form.code.value.trim();
    
    // Validate code format
    if (!/^\d{6}$/.test(code)) {
        showMessage('Verification code must be 6 digits', 'error');
        return;
    }
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    try {
        // Call the 2FA verification API
        const response = await callApi('/api/auth/2fa/verify', 'POST', {
            email,
            code
        });
        
        // Handle the response
        if (response.data.success) {
            showMessage(response.data.message || 'Verification successful!', 'success');
            
            // Save the token
            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
            }
            
            // Clear the session storage
            sessionStorage.removeItem('auth_email');
            
            // Redirect to dashboard after a short delay
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            showMessage(response.data.message || 'Invalid verification code. Please try again.', 'error');
        }
    } catch (error) {
        showMessage('An error occurred. Please try again later.', 'error');
        console.error('2FA verification error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
