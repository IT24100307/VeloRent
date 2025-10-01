$(document).ready(function() {
    // Elements
    const editBtn = $('#editBtn');
    const saveBtn = $('#saveBtn');
    const cancelBtn = $('#cancelBtn');
    const successAlert = $('#successAlert');
    const errorAlert = $('#errorAlert');
    const viewModeFields = $('.view-mode-fields');
    const viewModeLabels = $('.view-mode');
    const changePasswordBtn = $('#changePasswordBtn');
    const passwordSuccessAlert = $('#passwordSuccessAlert');
    const passwordErrorAlert = $('#passwordErrorAlert');

    // CSRF token for secure requests
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    // Pick email from query string if present (needed when viewing another profile or when not authenticated)
    const urlParams = new URLSearchParams(window.location.search);
    const queryEmail = urlParams.get('email');

    // Toggle Edit Mode
    editBtn.on('click', function() {
        enterEditMode();
    });

    // Cancel Edit
    cancelBtn.on('click', function() {
        exitEditMode();
    });

    // Save Profile Changes
    saveBtn.on('click', function() {
        saveProfileChanges();
    });

    // Change Password
    changePasswordBtn.on('click', function() {
        changePassword();
    });

    // Enter Edit Mode
    function enterEditMode() {
        viewModeFields.removeClass('d-none');
        viewModeLabels.addClass('d-none');
        editBtn.addClass('d-none');
        saveBtn.removeClass('d-none');
        cancelBtn.removeClass('d-none');
    }

    // Exit Edit Mode
    function exitEditMode() {
        viewModeFields.addClass('d-none');
        viewModeLabels.removeClass('d-none');
        editBtn.removeClass('d-none');
        saveBtn.addClass('d-none');
        cancelBtn.addClass('d-none');

        // Reset form to original values
        $('#profileForm')[0].reset();

        // Hide alerts
        successAlert.addClass('d-none');
        errorAlert.addClass('d-none');
    }

    // Save Profile Changes
    function saveProfileChanges() {
        // Show loading indicator or message
        errorAlert.addClass('d-none');
        successAlert.text("Saving your profile...");
        successAlert.removeClass('d-none');

        // Get form field values
        const firstName = $('#firstName').val().trim();
        const lastName = $('#lastName').val().trim();
        const currentEmail = $('#email').val().trim();
        const contactNumber = $('#contactNumber').length ? $('#contactNumber').val().trim() : null;
        const addressStreet = $('#addressStreet').length ? $('#addressStreet').val().trim() : null;
        const addressCity = $('#addressCity').length ? $('#addressCity').val().trim() : null;
        const addressPostalCode = $('#addressPostalCode').length ? $('#addressPostalCode').val().trim() : null;

        // Basic validation
        if (!firstName || !lastName) {
            errorAlert.text("First name and last name are required");
            errorAlert.removeClass('d-none');
            successAlert.addClass('d-none');
            return;
        }

        // Collect form data
        const profileData = {
            firstName: firstName,
            lastName: lastName,
            email: currentEmail,
            contactNumber: contactNumber,
            addressStreet: addressStreet,
            addressCity: addressCity,
            addressPostalCode: addressPostalCode
        };

        console.log("Updating profile with data:", profileData);

        // Create URL with email parameter if available (URL ?email or read-only #email field)
        let apiUrl = '/api/profile/update';
        const pageEmailVal = ($('#email').length ? $('#email').val().trim() : null);
        if (queryEmail) {
            apiUrl += '?email=' + encodeURIComponent(queryEmail);
        } else if (pageEmailVal) {
            apiUrl += '?email=' + encodeURIComponent(pageEmailVal);
        }

        console.log("Sending request to:", apiUrl);

        // Send AJAX request
        $.ajax({
            url: apiUrl,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(profileData),
            xhrFields: { withCredentials: true },
            headers: (function(){
                const h = {};
                const t = $('meta[name="_csrf"]').attr('content');
                if (t) { h['X-CSRF-TOKEN'] = t; }
                return h;
            })(),
            success: function(response) {
                console.log("Profile update response:", response);
                if (response.success) {
                    // Show success message
                    successAlert.text(response.message);
                    successAlert.removeClass('d-none');
                    errorAlert.addClass('d-none');

                    // Update view mode labels with new values
                    updateViewLabels(profileData);

                    // Exit edit mode after short delay
                    setTimeout(function() {
                        exitEditMode();
                        // Reload page to refresh data from server
                        location.reload();
                    }, 1500);
                } else {
                    // Show error message
                    errorAlert.text(response.message);
                    errorAlert.removeClass('d-none');
                    successAlert.addClass('d-none');
                }
            },
            error: function(xhr, status, error) {
                console.error("Profile update error:", status, error);
                console.error("Response:", xhr.responseText);

                let errorMessage = 'An error occurred while updating your profile.';
                try {
                    const responseJson = JSON.parse(xhr.responseText);
                    if (responseJson && responseJson.message) {
                        errorMessage = responseJson.message;
                    }
                } catch (e) {
                    // If JSON parsing fails, use the xhr.statusText
                    errorMessage = xhr.statusText || errorMessage;
                }

                errorAlert.text(errorMessage);
                errorAlert.removeClass('d-none');
                successAlert.addClass('d-none');
            }
        });
    }

    // Update view labels with new data
    function updateViewLabels(data) {
        $('#firstName').closest('.mb-3').find('.view-mode').text(data.firstName);
        $('#lastName').closest('.mb-3').find('.view-mode').text(data.lastName);

        if ($('#contactNumber').length) {
            const contactValue = data.contactNumber || 'Not provided';
            $('#contactNumber').closest('.mb-3').find('.view-mode').text(contactValue);
        }

        if ($('#addressStreet').length) {
            const streetValue = data.addressStreet || 'Not provided';
            $('#addressStreet').closest('.mb-3').find('.view-mode').text(streetValue);
        }

        if ($('#addressCity').length) {
            const cityValue = data.addressCity || 'Not provided';
            $('#addressCity').closest('.mb-3').find('.view-mode').text(cityValue);
        }

        if ($('#addressPostalCode').length) {
            const postalValue = data.addressPostalCode || 'Not provided';
            $('#addressPostalCode').closest('.mb-3').find('.view-mode').text(postalValue);
        }
    }

    // Password validation
    function validatePassword(password) {
        // Password must be at least 8 characters and include letters, numbers, and special characters
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        return passwordRegex.test(password);
    }

    // Change Password
    function changePassword() {
        // Reset validation states
        $('#currentPassword').removeClass('is-invalid');
        $('#newPassword').removeClass('is-invalid');
        $('#confirmPassword').removeClass('is-invalid');
        passwordErrorAlert.addClass('d-none');
        passwordSuccessAlert.addClass('d-none');

        // Get password values
        const currentPassword = $('#currentPassword').val();
        const newPassword = $('#newPassword').val();
        const confirmPassword = $('#confirmPassword').val();

        // Validate input
        let isValid = true;

        if (!currentPassword) {
            $('#currentPassword').addClass('is-invalid');
            isValid = false;
        }

        if (!validatePassword(newPassword)) {
            $('#newPassword').addClass('is-invalid');
            isValid = false;
        }

        if (newPassword !== confirmPassword) {
            $('#confirmPassword').addClass('is-invalid');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create URL with email parameter if available
        let apiUrl = '/api/profile/change-password';
        const pageEmailVal = ($('#email').length ? $('#email').val().trim() : null);
        if (queryEmail) {
            apiUrl += `?email=${encodeURIComponent(queryEmail)}`;
        } else if (pageEmailVal) {
            apiUrl += `?email=${encodeURIComponent(pageEmailVal)}`;
        }

        const headers = { 'Content-Type': 'application/json' };
        if (header && token) { headers[header] = token; }

        fetch(apiUrl, {
            method: 'POST',
            headers,
            credentials: 'same-origin',
            body: JSON.stringify({ currentPassword, newPassword, confirmPassword })
        })
        .then(async (r) => {
            let data = null;
            try { data = await r.json(); } catch(_) { data = { success: false, message: r.statusText || 'Request failed' }; }

            if (r.ok && data && data.success) {
                passwordSuccessAlert.text(data.message || 'Password updated successfully');
                passwordSuccessAlert.removeClass('d-none');
                passwordErrorAlert.addClass('d-none');
                $('#passwordForm')[0].reset();
                return;
            }

            const msg = (data && data.message) ? data.message : 'Error updating password';
            passwordErrorAlert.text(msg);
            passwordErrorAlert.removeClass('d-none');
            passwordSuccessAlert.addClass('d-none');
        })
        .catch((e) => {
            passwordErrorAlert.text('Network error while updating your password');
            passwordErrorAlert.removeClass('d-none');
            passwordSuccessAlert.addClass('d-none');
        })
        .finally(() => {
            changePasswordBtn.prop('disabled', false);
            changePasswordBtn.text('Change Password');
        });
    }
});
