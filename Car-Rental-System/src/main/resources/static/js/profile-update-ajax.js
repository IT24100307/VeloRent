/**
 * Profile Update Handler - Fixed version for saving profile changes
 * This implementation properly handles CSRF tokens and authentication
 */
document.addEventListener("DOMContentLoaded", function() {
    console.log("Profile update handler loaded - fixed version");

    // Find the save button and replace its click handler
    const saveButton = document.getElementById("save-profile-btn");
    if (saveButton) {
        // Replace the existing click handler with our direct implementation
        saveButton.addEventListener("click", function(e) {
            // Prevent the default form submission
            e.preventDefault();
            console.log("Save button clicked, preventing default form submission");

            // Run validation if available
            if (typeof validateForm === 'function') {
                if (!validateForm()) {
                    console.log("Form validation failed");
                    return;
                }
            }

            // Get user ID from the form
            const userId = document.getElementById("userId").value;
            console.log("Updating profile for user ID:", userId);

            // Build profile data object directly
            const profileData = {
                userId: userId,
                firstName: document.getElementById("firstName").value,
                lastName: document.getElementById("lastName").value,
                email: document.getElementById("email").value
            };

            // Add role name if available
            const roleNameField = document.getElementById("roleName");
            if (roleNameField) {
                profileData.roleName = roleNameField.value;
            }

            // Add password if provided
            const passwordField = document.getElementById("password");
            if (passwordField && passwordField.value) {
                const confirmField = document.getElementById("confirmPassword");
                if (confirmField && passwordField.value === confirmField.value) {
                    profileData.password = passwordField.value;
                } else {
                    showAlert("Passwords don't match", "danger");
                    return;
                }
            }

            // Add customer fields if they exist
            const contactNumberField = document.getElementById("contactNumber");
            if (contactNumberField) {
                profileData.contactNumber = contactNumberField.value;
                profileData.isCustomer = true;
            }

            const addressStreetField = document.getElementById("addressStreet");
            if (addressStreetField) {
                profileData.addressStreet = addressStreetField.value;
            }

            const addressCityField = document.getElementById("addressCity");
            if (addressCityField) {
                profileData.addressCity = addressCityField.value;
            }

            const addressPostalCodeField = document.getElementById("addressPostalCode");
            if (addressPostalCodeField) {
                profileData.addressPostalCode = addressPostalCodeField.value;
            }

            // Add staff fields if they exist
            const staffIdCodeField = document.getElementById("staffIdCode");
            if (staffIdCodeField) {
                profileData.staffIdCode = staffIdCodeField.value;
                profileData.isStaff = true;
            }

            const departmentField = document.getElementById("department");
            if (departmentField) {
                profileData.department = departmentField.value;
            }

            const positionField = document.getElementById("position");
            if (positionField) {
                profileData.position = positionField.value;
            }

            const employeeIdField = document.getElementById("employeeId");
            if (employeeIdField) {
                profileData.employeeId = employeeIdField.value;
            }

            // Show loading indicator
            showAlert("Saving changes...", "info");

            // Get CSRF token and header name
            const token = getCSRFToken();
            const headerName = getCSRFHeaderName();
            console.log("Using CSRF token:", token ? "Present" : "Missing");

            // Prepare headers
            const headers = {
                "Content-Type": "application/json"
            };

            // Only add CSRF token if available
            if (token) {
                headers[headerName] = token;
            }

            console.log("Sending profile update request with data:", JSON.stringify(profileData));

            // Use the correct endpoint that matches your backend controller
            console.log("Preparing to send update request to /profile/update");
            fetch("/profile/update", {
                method: "POST",
                headers: headers,
                body: JSON.stringify(profileData),
                credentials: 'same-origin' // Important for CSRF to work
            })
            .then(response => {
                console.log("Response status:", response.status);
                // First check if the response is JSON
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    return response.json().then(data => {
                        if (!response.ok) {
                            // Handle error with JSON response
                            console.error("Error response data:", data);
                            throw new Error(data.error || `Failed to update profile: HTTP ${response.status}`);
                        }
                        return data;
                    });
                } else {
                    // For HTML responses (common in Spring MVC), check status code
                    if (!response.ok) {
                        throw new Error(`Failed to update profile: HTTP ${response.status}`);
                    }
                    // For non-JSON success responses, return empty object
                    return {};
                }
            })
            .then(data => {
                console.log("Profile updated successfully", data);

                // Show success message
                showAlert("Profile updated successfully!", "success");

                // Update the UI to show changes if data is available
                if (data.firstName && data.lastName && document.getElementById("user-name-dropdown")) {
                    document.getElementById("user-name-dropdown").textContent =
                        data.firstName + " " + data.lastName;

                    // Update localStorage with new user data
                    localStorage.setItem("userName", data.firstName + " " + data.lastName);
                }

                if (data.email && document.getElementById("user-email-dropdown")) {
                    document.getElementById("user-email-dropdown").textContent = data.email;

                    // Update localStorage with new email
                    localStorage.setItem("userEmail", data.email);
                }

                // Return to view mode
                disableEditMode();

                // Reload the page after a short delay to refresh data
                setTimeout(() => {
                    window.location.href = "/profile?success=true";
                }, 1500);
            })
            .catch(error => {
                console.error("Error updating profile:", error);
                showAlert("Error: " + error.message, "danger");

                // Don't disable edit mode on error so user can try again
            });
        }, true); // Use capturing to ensure this runs before other handlers
    }
});

// Helper function to get CSRF token from meta tag
function getCSRFToken() {
    const metaTag = document.querySelector('meta[name="_csrf"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }

    // If meta tag not found, try to get from input field (for backward compatibility)
    const inputTag = document.querySelector('input[name="_csrf"]');
    if (inputTag) {
        return inputTag.value;
    }

    console.warn("CSRF token not found");
    return '';
}

// Helper function to get CSRF header name
function getCSRFHeaderName() {
    const metaTag = document.querySelector('meta[name="_csrf_header"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }

    return 'X-CSRF-TOKEN'; // Default header name
}

// Helper function to show alerts if not already defined
if (typeof showAlert !== 'function') {
    function showAlert(message, type) {
        const alertContainer = document.getElementById("alert-container");
        if (alertContainer) {
            alertContainer.innerHTML = `
                <div class="alert alert-${type}">
                    ${message}
                </div>
            `;

            // Auto-hide the alert after 5 seconds
            setTimeout(() => {
                alertContainer.innerHTML = "";
            }, 5000);
        } else {
            console.log(`Alert (${type}): ${message}`);
        }
    }
}
