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
        if (window.showLuxuryMessage) {
            window.showLuxuryMessage('Verification code must be exactly 6 digits', 'error');
        } else {
            showMessage('Verification code must be 6 digits', 'error');
        }
        return;
    }
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    // Show loading message
    if (window.showLuxuryMessage) {
        window.showLuxuryMessage('Verifying your code...', 'info');
    }
    
    try {
        // Call the 2FA verification API
        const response = await callApi('/api/auth/2fa/verify', 'POST', {
            email,
            code
        });
        
        // Handle the response
        if (response.data.success) {
            if (window.showLuxuryMessage) {
                window.showLuxuryMessage(response.data.message || 'Verification successful! Redirecting...', 'success');
            } else {
                showMessage(response.data.message || 'Verification successful!', 'success');
            }
            
            // Clear any existing user data to prevent conflicts between different accounts
            localStorage.clear();
            
            // Save the token
            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
            }
            
            // Save user role if available
            if (response.data.role) {
                localStorage.setItem('userRole', response.data.role);
            }

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

            // Clear the session storage
            sessionStorage.removeItem('auth_email');
            
            // Determine redirect URL based on user role
            let redirectUrl = '/dashboard'; // Default dashboard

            // Check user role and redirect accordingly
            if (response.data.role) {
                const role = response.data.role;
                if (role.includes('FLEET_MANAGER')) {
                    redirectUrl = '/fleet-manager/dashboard';
                } else if (role.includes('ADMIN') || role.includes('OWNER')) {
                    redirectUrl = '/admin/dashboard';
                }
            }

            console.log('Role after 2FA:', response.data.role);
            console.log('Redirecting to:', redirectUrl);

            // Redirect to appropriate dashboard after a short delay
            setTimeout(() => {
                window.location.href = redirectUrl;
            }, 1000);
        } else {
            if (window.showLuxuryMessage) {
                window.showLuxuryMessage(response.data.message || 'Invalid verification code. Please check your authenticator app and try again.', 'error');
            } else {
                showMessage(response.data.message || 'Invalid verification code. Please try again.', 'error');
            }
        }
    } catch (error) {
        if (window.showLuxuryMessage) {
            window.showLuxuryMessage('Network error occurred. Please check your connection and try again.', 'error');
        } else {
            showMessage('An error occurred. Please try again later.', 'error');
        }
        console.error('2FA verification error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
