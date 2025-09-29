/**
 * Direct Profile Update - Complete replacement for profile update functionality
 * This script bypasses the traditional form submission that's causing 403 errors
 */
document.addEventListener("DOMContentLoaded", function() {
    // Find important elements
    const profileForm = document.getElementById("profile-form");
    const saveButton = document.getElementById("save-profile-btn");
    const editButton = document.getElementById("edit-profile-btn");
    const cancelButton = document.getElementById("cancel-btn");

    if (saveButton) {
        // Replace the existing click handler with our direct implementation
        saveButton.onclick = function(e) {
            e.preventDefault();
            e.stopImmediatePropagation(); // Stop other handlers from running

            // Check validation if available
            if (typeof validateForm === 'function' && !validateForm()) {
                return false;
            }

            // Get user ID from the form
            const userId = document.getElementById("userId").value;

            // Build data object directly instead of using the form
            const profileData = {
                userId: userId,
                firstName: document.getElementById("firstName").value,
                lastName: document.getElementById("lastName").value,
                email: document.getElementById("email").value,
                roleName: document.getElementById("roleName").value
            };

            // Add password if provided
            const password = document.getElementById("password").value;
            if (password) {
                const confirmPassword = document.getElementById("confirmPassword").value;
                if (password !== confirmPassword) {
                    showAlert("Passwords don't match", "danger");
                    return false;
                }
                profileData.password = password;
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

            // Show loading message
            showAlert("Saving profile changes...", "info");

            // Send data directly to API endpoint
            fetch("/profile/api/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("token")
                },
                body: JSON.stringify(profileData)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || "Failed to update profile");
                    });
                }
                return response.json();
            })
            .then(data => {
                showAlert("Profile updated successfully!", "success");

                // Update page data without refreshing
                document.getElementById("firstName").value = data.firstName || profileData.firstName;
                document.getElementById("lastName").value = data.lastName || profileData.lastName;
                document.getElementById("email").value = data.email || profileData.email;

                // Clear password fields
                if (document.getElementById("password")) {
                    document.getElementById("password").value = "";
                }

                if (document.getElementById("confirmPassword")) {
                    document.getElementById("confirmPassword").value = "";
                }

                // Update name in top navbar if it exists
                const userNameElement = document.getElementById("user-name");
                if (userNameElement) {
                    userNameElement.textContent = data.firstName + " " + data.lastName;
                }

                // Update name in dropdown
                const userNameDropdown = document.getElementById("user-name-dropdown");
                if (userNameDropdown) {
                    userNameDropdown.textContent = data.firstName + " " + data.lastName;
                }

                // Update email in dropdown
                const userEmailDropdown = document.getElementById("user-email-dropdown");
                if (userEmailDropdown) {
                    userEmailDropdown.textContent = data.email;
                }

                // Update localStorage
                localStorage.setItem("userName", data.firstName + " " + data.lastName);
                localStorage.setItem("userEmail", data.email);

                // Return to view mode
                disableEditMode();
            })
            .catch(error => {
                console.error("Error updating profile:", error);
                showAlert("Error: " + error.message, "danger");
            });

            return false; // Prevent default form submission
        };
    }

    // Custom alert function (if not already defined)
    function showAlert(message, type) {
        const alertContainer = document.getElementById("alert-container");
        if (alertContainer) {
            alertContainer.innerHTML = `
                <div class="alert alert-${type}">
                    ${message}
                </div>
            `;

            if (type === "success" || type === "info") {
                setTimeout(() => {
                    alertContainer.innerHTML = "";
                }, 5000);
            }
        } else {
            alert(message);
        }
    }
});

