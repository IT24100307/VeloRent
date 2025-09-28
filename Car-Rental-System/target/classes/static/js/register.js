// register.js - Handles the registration form functionality

document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('register-form');
    const roleNameSelect = document.getElementById('roleName');
    const customerDetailsSection = document.getElementById('customerDetailsSection');
    const staffDetailsSection = document.getElementById('staffDetailsSection');

    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }

    if (roleNameSelect) {
        // Show/hide appropriate details section based on selected role
        roleNameSelect.addEventListener('change', () => {
            const selectedRole = roleNameSelect.value;
            if (selectedRole === 'ROLE_CUSTOMER') {
                customerDetailsSection.style.display = 'block';
                staffDetailsSection.style.display = 'none';
            } else {
                customerDetailsSection.style.display = 'none';
                staffDetailsSection.style.display = 'block';
            }
        });

        // Trigger the change event to set the initial state
        roleNameSelect.dispatchEvent(new Event('change'));
    }
});

/**
 * Validates the registration form
 * @param {HTMLFormElement} form - The form to validate
 * @returns {boolean} - Whether the form is valid
 */
function validateForm(form) {
    const email = form.email.value.trim();
    const password = form.password.value;
    const passwordConfirm = form.passwordConfirm.value;
    const roleName = form.roleName.value;

    // Email validation
    if (!isValidEmail(email)) {
        showMessage('Please enter a valid email address', 'error');
        return false;
    }

    // Password validation
    if (password.length < 8) {
        showMessage('Password must be at least 8 characters long', 'error');
        return false;
    }

    // Password confirmation
    if (password !== passwordConfirm) {
        showMessage('Passwords do not match', 'error');
        return false;
    }

    // Role-specific validation
    if (roleName === 'ROLE_CUSTOMER') {
        // Validate customer fields (making them optional for now)
        // You can make them required if needed
    } else {
        // Staff ID code is required for admin roles (it now serves as registration code too)
        if (!form.staffIdCode.value.trim()) {
            showMessage('Staff ID Code is required for administrator accounts', 'error');
            return false;
        }
    }

    return true;
}

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

    // Confirm passwords match
    if (password !== passwordConfirm) {
        showMessage('Passwords do not match', 'error');
        return;
    }

    // Set button to loading state
    setButtonLoading(submitButton, true);

    try {
        // Build the request data based on the selected role
        const requestData = {
            firstName,
            lastName,
            email,
            password,
            roleName
        };

        // Add role-specific fields
        if (roleName === 'ROLE_CUSTOMER') {
            // Add customer details
            requestData.contactNumber = form.contactNumber.value.trim();
            requestData.addressStreet = form.addressStreet.value.trim();
            requestData.addressCity = form.addressCity.value.trim();
            requestData.addressPostalCode = form.addressPostalCode.value.trim();
        } else {
            // Add staff details and use staff ID code as registration code
            const staffIdCode = form.staffIdCode.value.trim();
            requestData.registrationCode = staffIdCode;
            requestData.staffIdCode = staffIdCode;
        }

        // Call the registration API
        const response = await callApi('/api/auth/register', 'POST', requestData);

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
        showMessage('Network error. Please try again.', 'error');
        console.error('Registration error:', error);
    } finally {
        // Reset button state
        setButtonLoading(submitButton, false);
    }
}
