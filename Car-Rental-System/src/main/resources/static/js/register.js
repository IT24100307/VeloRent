// register.js - Handles the registration form functionality

document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('register-form');
    const roleNameSelect = document.getElementById('roleName');
    const registrationCodeGroup = document.getElementById('registrationCodeGroup');
    
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }
    
    if (roleNameSelect && registrationCodeGroup) {
        // Show/hide registration code field based on selected role
        roleNameSelect.addEventListener('change', () => {
            const selectedRole = roleNameSelect.value;
            if (selectedRole === 'ROLE_CUSTOMER') {
                registrationCodeGroup.style.display = 'none';
            } else {
                registrationCodeGroup.style.display = 'block';
            }
        });
    }
});

/**
 * Handles the registration form submission
 * @param {Event} event - The form submission event
 */
async function handleRegistration(event) {
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
    const firstName = form.firstName.value.trim();
    const lastName = form.lastName.value.trim();
    const email = form.email.value.trim();
    const password = form.password.value;
    const passwordConfirm = form.passwordConfirm.value;
    const roleName = form.roleName.value;
    const registrationCode = form.registrationCode.value;
    
    // Confirm passwords match
    if (password !== passwordConfirm) {
        showMessage('Passwords do not match', 'error');
        return;
    }
    
    // Set button to loading state
    setButtonLoading(submitButton, true);
    
    try {
        // Call the registration API
        const response = await callApi('/api/auth/register', 'POST', {
            firstName,
            lastName,
            email,
            password,
            roleName,
            registrationCode: roleName !== 'ROLE_CUSTOMER' ? registrationCode : undefined
        });
        
        // Handle the response
        if (response.data.success) {
            showMessage(response.data.message || 'Registration successful! You can now login.', 'success');
            
            // Clear form
            form.reset();
            
            // Redirect to login after a short delay
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            showMessage(response.data.message || 'Registration failed. Please try again.', 'error');
        }
    } catch (error) {
        showMessage('An error occurred. Please try again later.', 'error');
        console.error('Registration error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
