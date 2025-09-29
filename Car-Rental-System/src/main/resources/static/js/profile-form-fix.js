/**
 * Profile Form Fix - Fixes issues with form submission for profile updates
 */
document.addEventListener("DOMContentLoaded", function() {
    // Find the form and buttons
    const profileForm = document.getElementById("profile-form");
    const saveButton = document.getElementById("save-profile-btn");

    if (profileForm && saveButton) {
        // Override the existing click event with our fixed version
        saveButton.addEventListener("click", function(e) {
            e.preventDefault();

            // Run any existing validation
            if (typeof validateForm === "function") {
                if (!validateForm()) {
                    return false;
                }
            }

            // Enable all disabled form fields so their values get submitted
            const disabledFields = profileForm.querySelectorAll("input:disabled, select:disabled, textarea:disabled");
            disabledFields.forEach(field => {
                // Remember which fields were disabled so we can restore them later
                field.setAttribute("data-was-disabled", "true");
                field.disabled = false;
            });

            // Submit the form
            profileForm.submit();

            // Re-disable fields that were originally disabled
            // (Though the page will reload so this may not be necessary)
            disabledFields.forEach(field => {
                if (field.getAttribute("data-was-disabled") === "true") {
                    field.disabled = true;
                }
            });

            return false;
        }, true); // Use capturing to ensure this runs before other event handlers
    }
});

