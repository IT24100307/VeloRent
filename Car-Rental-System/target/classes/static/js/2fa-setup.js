// 2fa-setup.js - Handles the 2FA setup functionality

document.addEventListener('DOMContentLoaded', () => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login?message=' + encodeURIComponent('Please log in to access this page') + '&type=error';
        return;
    }

    const enableBtn = document.getElementById('enable-2fa-btn');
    const setupContainer = document.getElementById('setup-container');
    const qrContainer = document.getElementById('qr-container');
    const verifyForm = document.getElementById('verify-2fa-form');
    
    if (enableBtn) {
        enableBtn.addEventListener('click', setupTwoFactorAuth);
    }
    
    if (verifyForm) {
        verifyForm.addEventListener('submit', verifyAndEnable2FA);
    }
});

/**
 * Sets up two-factor authentication
 */
async function setupTwoFactorAuth() {
    const enableBtn = document.getElementById('enable-2fa-btn');
    const setupContainer = document.getElementById('setup-container');
    const qrContainer = document.getElementById('qr-container');
    
    // Clear previous messages
    clearMessage();
    
    // Set button to loading state
    setButtonLoading(enableBtn, true);
    
    try {
        console.log('Attempting to set up 2FA...');
        
        // Get email from localStorage if available
        const userEmail = localStorage.getItem('userEmail');
        console.log('User email from localStorage:', userEmail);
        
        // Call the 2FA setup API
        const response = await callApi('/api/auth/2fa/setup', 'POST');
        
        console.log('2FA setup response status:', response.status);
        console.log('2FA setup response data:', response.data);
        
        if (response.status === 200 && response.data.qrCodeUri) {
            console.log('QR code URI received, length:', response.data.qrCodeUri.length);
            console.log('QR code URI start:', response.data.qrCodeUri.substring(0, 50) + '...');
            
            // Show QR code and secret key
            const qrCodeImg = document.getElementById('qr-code');
            const qrError = document.getElementById('qr-error');
            const qrLoading = document.getElementById('qr-loading');
            
            // Show loading indicator
            qrLoading.style.display = 'block';
            qrError.style.display = 'none';
            qrCodeImg.style.display = 'none';
            
            // Verify if we have a valid QR code URI
            if (response.data.qrCodeUri && response.data.qrCodeUri.startsWith('data:image')) {
                // If it's a Data URI, set it directly
                qrCodeImg.src = response.data.qrCodeUri;
            } else if (response.data.qrCodeUri) {
                // If it's a URL to an image, we need to handle potential CORS issues
                // First try to convert it to a data URI using a canvas
                try {
                    console.log('QR code URI received, length:', response.data.qrCodeUri.length);
                    // For debugging, log a portion of the URI (first 100 chars)
                    console.log('QR code URI start:', response.data.qrCodeUri.substring(0, 100));
                    qrCodeImg.src = response.data.qrCodeUri;
                } catch (e) {
                    console.error('Error setting QR code image source:', e);
                    qrError.style.display = 'block';
                    qrLoading.style.display = 'none';
                }
            } else {
                // No valid QR code URI
                console.error('No valid QR code URI received');
                qrError.style.display = 'block';
                qrLoading.style.display = 'none';
            }
            
            // Add error handler to the image to detect loading issues
            qrCodeImg.onerror = function() {
                console.error('Failed to load QR code image');
                qrError.style.display = 'block';
                qrLoading.style.display = 'none';
                qrCodeImg.style.display = 'none';
                showMessage('Failed to load QR code image. Please try again.', 'error');
            };
            
            // Add load handler
            qrCodeImg.onload = function() {
                console.log('QR code image loaded successfully');
                qrLoading.style.display = 'none';
                qrError.style.display = 'none';
                qrCodeImg.style.display = 'block';
            };
            
            // Extract secret from QR URI (typically after 'secret=')
            const qrUri = response.data.qrCodeUri;
            const secretMatch = qrUri.match(/secret=([A-Z0-9]+)/);
            if (secretMatch && secretMatch[1]) {
                document.getElementById('secret-key').textContent = secretMatch[1];
                console.log('Secret key extracted and displayed');
            } else {
                console.error('Could not extract secret key from QR URI');
            }
            
            // Hide setup button and show QR container
            setupContainer.style.display = 'none';
            qrContainer.style.display = 'block';
            
            showMessage(response.data.message || 'Scan the QR code with your authenticator app.', 'success');
        } else {
            console.error('Failed to set up 2FA:', response);
            showMessage(response.data.message || 'Failed to set up 2FA. Please try again.', 'error');
        }
    } catch (error) {
        console.error('2FA setup error:', error);
        showMessage('An error occurred. Please try again later. Check console for details.', 'error');
    } finally {
        // Reset button state
        setButtonLoading(enableBtn, false);
    }
}

/**
 * Verifies the 2FA code and enables 2FA for the user
 * @param {Event} event - The form submission event
 */
async function verifyAndEnable2FA(event) {
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
    const code = form.code.value.trim();
    
    // Get email from localStorage or sessionStorage
    const email = localStorage.getItem('userEmail') || sessionStorage.getItem('auth_email');
    
    if (!email) {
        showMessage('Authentication issue. Please try logging in again.', 'error');
        return;
    }
    
    // Validate code format
    if (!/^\d{6}$/.test(code)) {
        showMessage('Verification code must be 6 digits', 'error');
        return;
    }
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    try {
        console.log('Sending verification with email:', email);
        // Call the 2FA enable API
        const response = await callApi('/api/auth/2fa/enable', 'POST', { 
            code,
            email
        });
        
        // Handle the response
        if (response.data.success) {
            showMessage(response.data.message || '2FA enabled successfully!', 'success');
            
            // Redirect to dashboard after a short delay
            setTimeout(() => {
                window.location.href = '/dashboard?message=' + encodeURIComponent('Two-factor authentication has been enabled for your account.') + '&type=success';
            }, 2000);
        } else {
            showMessage(response.data.message || 'Failed to enable 2FA. Please try again.', 'error');
        }
    } catch (error) {
        showMessage('An error occurred. Please try again later.', 'error');
        console.error('2FA enable error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
