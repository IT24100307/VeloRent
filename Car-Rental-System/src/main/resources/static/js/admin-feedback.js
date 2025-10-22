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
    const dt = $('#feedbackTable').DataTable({
        dom: 'Bfrtip',
        language: { emptyTable: "No Feedback Available" },
        stateSave: true,
        buttons: [
            { extend: 'colvis', collectionLayout: 'fixed two-column', text: 'Column Visibility' },
            { extend: 'copyHtml5', text: 'Copy' },
            { extend: 'excelHtml5', text: 'Excel' },
            { extend: 'csvHtml5', text: 'CSV' }
        ]
    });

    // Hook up filter chips
    const $chips = $('#feedback-filter-bar .filter-chip');
    $chips.on('click', function(){
        $chips.removeClass('active');
        $(this).addClass('active');
        const type = $(this).data('filter');

        // Custom filtering using row attributes
        $.fn.dataTable.ext.search = $.fn.dataTable.ext.search.filter(fn => !fn.__feedbackFilter);
        const filterFn = function(settings, data, dataIndex){
            const row = dt.row(dataIndex).node();
            const rating = parseInt(row.getAttribute('data-rating') || '0', 10);
            const replied = (row.getAttribute('data-replied') || '0') === '1';
            switch(type){
                case 'rating-5': return rating === 5;
                case 'rating-4plus': return rating >= 4;
                case 'needs-reply': return !replied;
                case 'replied': return replied;
                default: return true;
            }
        };
        filterFn.__feedbackFilter = true;
        $.fn.dataTable.ext.search.push(filterFn);
        dt.draw();
    });

    // Search highlight on draw/search
    dt.on('draw.dt search.dt', function(){
        const term = dt.search();
        $('#feedbackTable tbody').unhighlight && $('#feedbackTable tbody').unhighlight();
        if (term && window.jQuery && jQuery.fn.highlight){
            $('#feedbackTable tbody').highlight(term);
        }
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

    // Show more/less for comment
    $('#feedbackTable').on('click', '.show-toggle', function(){
        const $row = $(this).closest('td');
        const $text = $row.find('.comment-text');
        const expanded = !$text.hasClass('truncate-2');
        if (expanded){
            $text.addClass('truncate-2');
            $(this).text('Show more');
        } else {
            $text.removeClass('truncate-2');
            $(this).text('Show less');
        }
    });

    // Quick view modal
    const modalEl = document.getElementById('feedbackQuickView');
    let bsModal = null;
    if (modalEl && window.bootstrap){
        bsModal = new bootstrap.Modal(modalEl);
    }
    $('#feedbackTable').on('click', '.quick-view-btn', function(){
        if (!bsModal) return;
        const d = this.dataset;
        $('#qvName').text(d.name || '');
        $('#qvDate').text(d.date || '');
        $('#qvComments').text(d.comments || '');
        if (d.reply){
            $('#qvReplyWrap').show();
            $('#qvReply').text(d.reply);
        } else {
            $('#qvReplyWrap').hide();
            $('#qvReply').text('');
        }
        // stars
        const r = parseInt(d.rating || '0', 10);
        const stars = Array.from({length:5}, (_,i)=>`<i class="${i<r?'fas':'far'} fa-star"></i>`).join('');
        $('#qvStars').html(stars);
        bsModal.show();
    });

    // Scroll to top
    const $scroll = $('#scrollTopBtn');
    $(window).on('scroll', function(){
        if (window.scrollY > 300) { $scroll.fadeIn(150); } else { $scroll.fadeOut(150); }
    });
    $scroll.on('click', function(){ $('html, body').animate({scrollTop: 0}, 400); });
}

/* Lightweight jQuery highlight plugin (no external dependency) */
(function($){
    if (!$) return;
    $.fn.highlight = function(pat){
        const inner = function(node){
            let skip = 0;
            if (node.nodeType === 3){
                const pos = node.data.toLowerCase().indexOf(pat.toLowerCase());
                if (pos >= 0 && node.data.trim() !== ''){
                    const spannode = document.createElement('mark');
                    spannode.style.background = 'rgba(212,175,55,0.35)';
                    spannode.style.color = 'inherit';
                    const middlebit = node.splitText(pos);
                    const endbit = middlebit.splitText(pat.length);
                    const middleclone = middlebit.cloneNode(true);
                    spannode.appendChild(middleclone);
                    middlebit.parentNode.replaceChild(spannode, middlebit);
                    skip = 1;
                }
            } else if (node.nodeType === 1 && node.childNodes && !/(script|style)/i.test(node.tagName)){
                for (let i=0; i<node.childNodes.length; i++){
                    i += inner(node.childNodes[i]);
                }
            }
            return skip;
        };
        return this.each(function(){ inner(this); });
    };
    $.fn.unhighlight = function(){
        return this.find('mark').each(function(){
            this.outerHTML = this.innerHTML;
        }).end();
    };
})(window.jQuery);
