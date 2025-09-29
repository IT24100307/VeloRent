/**
 * Profile Update Override - This script will override the default behavior of the save button
 * to fix the 403 "Access Denied" error when updating profiles.
 */
window.addEventListener("load", function() {
    console.log("Profile Update Override loaded");

    // Wait a short moment for all other scripts to initialize
    setTimeout(() => {
        // Find the save button and replace its click handler
        const saveButton = document.getElementById("save-profile-btn");
        if (saveButton) {
            console.log("Found save button, overriding behavior");

            // Completely override the onclick handler
            saveButton.onclick = function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log("Save button clicked, using custom handler");

                // Enable all disabled form fields before collecting data
                const disabledFields = document.querySelectorAll("#profile-form input:disabled, #profile-form select:disabled");
                disabledFields.forEach(field => {
                    field.disabled = false;
                });

                // Get user ID from the form
                const userId = document.getElementById("userId").value;

                // Build profile data object
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
                        alert("Passwords don't match");
                        // Re-disable fields
                        disabledFields.forEach(field => field.disabled = true);
                        return false;
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

                // Show alert message
                if (typeof showAlert === 'function') {
                    showAlert("Saving changes...", "info");
                } else {
                    console.log("Saving profile changes...");
                }

                // Send data directly to the API endpoint
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
                        throw new Error("Failed to update profile: HTTP " + response.status);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log("Profile updated successfully", data);

                    // Update localStorage with new user data
                    localStorage.setItem("userName", data.firstName + " " + data.lastName);
                    localStorage.setItem("userEmail", data.email);

                    // Show success message and reload the page
                    if (typeof showAlert === 'function') {
                        showAlert("Profile updated successfully!", "success");
                    } else {
                        alert("Profile updated successfully!");
                    }

                    // Reload the page after a short delay
                    setTimeout(() => {
                        window.location.href = "/profile?success=true";
                    }, 1000);
                })
                .catch(error => {
                    console.error("Error updating profile:", error);

                    // Show error message
                    if (typeof showAlert === 'function') {
                        showAlert("Error updating profile: " + error.message, "danger");
                    } else {
                        alert("Error updating profile: " + error.message);
                    }

                    // Re-disable fields
                    disabledFields.forEach(field => field.disabled = true);
                });

                return false;
            };
        } else {
            console.warn("Save button not found, profile update override not applied");
        }
    }, 500);
});

