/**
 * Admin Feedback Management JavaScript
 * This script handles all feedback-specific functionality for the admin dashboard
 */

$(document).ready(function() {
    // Only initialize feedback functionality if we're on the feedback page
    if (window.location.pathname.includes('/admin/feedback')) {
        initFeedbackTable();
        setupFeedbackEvents();
    }
});

function initFeedbackTable() {
    $('#feedbackTable').DataTable({
        dom: 'Bfrtip',
        language: {
            emptyTable: "No Feedback Available"
        },
        buttons: [
            {
                extend: 'colvis',
                collectionLayout: 'fixed two-column'
            },
            'copyHtml5',
            'excelHtml5',
            'csvHtml5',
        ]
    });
}

function setupFeedbackEvents() {
    // Handle reply form submission via AJAX
    $('.feedback-reply-form').on('submit', function(e) {
        e.preventDefault();
        const form = $(this);

        $.ajax({
            url: form.attr('action'),
            type: 'POST',
            data: form.serialize(),
            success: function() {
                window.location.reload();
            },
            error: function() {
                alert('Error submitting reply');
            }
        });
    });
}
