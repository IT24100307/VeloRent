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
 * Sets up two-factor authentication with improved error handling
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
        
        // Get user data from localStorage
        const userEmail = localStorage.getItem('userEmail');
        const token = localStorage.getItem('token');

        console.log('User email from localStorage:', userEmail);
        
        // Validate that we have the necessary data
        if (!userEmail) {
            // Try to get email from the page if available
            const emailField = document.querySelector('input[type="email"]');
            if (emailField && emailField.value) {
                localStorage.setItem('userEmail', emailField.value);
                console.log('Email retrieved from input field:', emailField.value);
            } else {
                throw new Error('User email not found. Please log out and log in again.');
            }
        }

        if (!token) {
            throw new Error('Authentication token not found. Please log out and log in again.');
        }

        // Prepare request data with all available user information
        const requestData = {
            email: userEmail,
            timestamp: new Date().getTime() // Add timestamp to prevent caching
        };

        // Call the 2FA setup API
        console.log('Sending 2FA setup request with data:', requestData);
        const response = await callApi('/api/auth/2fa/setup', 'POST', requestData);

        console.log('2FA setup response status:', response.status);
        console.log('2FA setup response data:', response.data);

        // Process the response
        if (response.status === 200 && response.data && response.data.qrCodeUri) {
            console.log('QR code URI received');

            // Show QR code and secret key
            const qrCodeImg = document.getElementById('qr-code');
            const qrError = document.getElementById('qr-error');
            const qrLoading = document.getElementById('qr-loading');
            
            // Show loading indicator
            qrLoading.style.display = 'block';
            qrError.style.display = 'none';
            qrCodeImg.style.display = 'none';
            
            // Set the QR code image source
            try {
                // For debugging
                if (typeof response.data.qrCodeUri !== 'string') {
                    throw new Error('QR code URI is not a string');
                }

                // Process the QR code URI based on what the backend provides
                let qrUri = response.data.qrCodeUri;

                // Debug the QR URI to see what format we're getting
                console.log('Raw QR URI type:', typeof qrUri);
                console.log('Raw QR URI prefix:', qrUri.substring(0, 30));

                if (qrUri.startsWith('otpauth://')) {
                    // This is a TOTP URI directly - we need to generate a QR code from it
                    // Use a public API to generate the QR code from the TOTP URI
                    const encodedUri = encodeURIComponent(qrUri);
                    qrUri = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodedUri}`;
                    console.log('Using QR API with TOTP URI');
                } else if (!qrUri.startsWith('data:image')) {
                    // If it's raw Base64 data without proper data URI format
                    if (qrUri.startsWith('/9j/') || qrUri.startsWith('iVBOR')) {
                        qrUri = 'data:image/png;base64,' + qrUri;
                        console.log('Converted raw Base64 to data URI');
                    }
                }

                // Set image source
                qrCodeImg.src = qrUri;
                console.log('QR code image source set to:', qrUri.substring(0, 50) + '...');

                // Make sure the QR code is visible with proper settings
                qrCodeImg.style.display = 'block';
                qrCodeImg.style.margin = '0 auto';
                qrCodeImg.style.width = '200px';
                qrCodeImg.style.height = 'auto';
                // Ensure the QR code has good contrast for scanning
                qrCodeImg.style.backgroundColor = 'white';
                qrCodeImg.style.padding = '10px';

                // Hide loading
                qrLoading.style.display = 'none';

                // Add load handler
                qrCodeImg.onload = function() {
                    console.log('QR code image loaded successfully');
                    qrLoading.style.display = 'none';
                    qrError.style.display = 'none';
                };

                // Add error handler to the image to detect loading issues
                qrCodeImg.onerror = function() {
                    console.error('Failed to load QR code image');
                    qrError.style.display = 'block';
                    qrLoading.style.display = 'none';
                    qrCodeImg.style.display = 'none';
                    showMessage('Failed to load QR code image. Please try again.', 'error');
                };

                // Extract and display the secret key
                if (response.data.secretKey) {
                    // If the backend provides the secret key directly, use it
                    document.getElementById('secret-key').textContent = response.data.secretKey;
                    console.log('Secret key displayed');
                } else {
                    // Try to extract secret from QR URI (typically after 'secret=')
                    const secretMatch = qrUri.match(/secret=([A-Z0-9]+)/);
                    if (secretMatch && secretMatch[1]) {
                        document.getElementById('secret-key').textContent = secretMatch[1];
                        console.log('Secret key extracted and displayed');
                    } else {
                        console.error('Could not extract secret key from QR URI');
                        document.getElementById('secret-key').textContent = 'Secret key not available';
                    }
                }

                // Hide setup button and show QR container
                setupContainer.style.display = 'none';
                qrContainer.style.display = 'block';

                showMessage('Scan the QR code with your authenticator app.', 'success');

            } catch (e) {
                console.error('Error setting QR code image source:', e);
                qrError.textContent = 'Error: ' + e.message;
                qrError.style.display = 'block';
                qrLoading.style.display = 'none';
                showMessage('Failed to load QR code. Please try refreshing the page.', 'error');
            }
        } else {
            // Handle specific error cases
            console.error('Failed to set up 2FA:', response);
            let errorMsg = response.data?.message || 'Failed to set up 2FA. Please try again.';

            if (response.status === 401) {
                errorMsg = 'Your session has expired. Please log out and log in again.';
                // Force logout after a delay
                setTimeout(() => {
                    localStorage.removeItem('token');
                    localStorage.removeItem('userRole');
                    window.location.href = '/login?message=' + encodeURIComponent('Session expired. Please log in again.') + '&type=error';
                }, 3000);
            } else if (response.status === 404) {
                errorMsg = 'User account not found. Please check your login credentials.';
            } else if (response.status === 500) {
                errorMsg = 'Server error generating QR code. Please contact support.';
            }

            showMessage(errorMsg, 'error');
        }
    } catch (error) {
        console.error('2FA setup error:', error);
        showMessage(error.message || 'An error occurred connecting to the server. Please check your connection and try again.', 'error');
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
