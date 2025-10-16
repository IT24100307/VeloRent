(function(){
  let dt = null;
  let actionsWired = false;

  function fmtCurrency(n){
    if(n===null||n===undefined) return '-';
    try{ return new Intl.NumberFormat(undefined,{style:'currency',currency:'USD'}).format(Number(n)); }catch(e){ return '$'+Number(n).toFixed(2); }
  }
  function fmtDate(d){
    if(!d) return '-';
    try{ return new Date(d).toLocaleString(); }catch(e){ return d; }
  }
  function badge(status){
    const s=(status||'').toLowerCase();
    if(s.includes('pend')) return '<span class="badge pending">Pending</span>';
    if(s.includes('cancel')) return '<span class="badge failed">Cancelled</span>';
    if(s.includes('fail')||s.includes('declin')) return '<span class="badge failed">Failed</span>';
    return '<span class="badge success">Completed</span>';
  }

  function actionButtons(p){
    const method = (p.paymentMethod||'').toLowerCase();
    const status = (p.paymentStatus||'').toLowerCase();
    const pieces = [];
    if(method==='cash' && status.includes('pend')){
      pieces.push(`<button class="btn btn-sm btn-success js-confirm" data-id="${p.paymentId}"><i class="fa fa-check"></i> Confirm</button>`);
    }
    if(method==='card' || method==='cash'){
      pieces.push(`<button class="btn btn-sm btn-danger js-cancel" data-id="${p.paymentId}"><i class="fa fa-times"></i> Cancel</button>`);
    }
    pieces.push(`<button class="btn btn-sm btn-outline-danger js-delete" data-id="${p.paymentId}"><i class="fa fa-trash"></i> Delete</button>`);
    return pieces.join(' ');
  }

  async function loadSummary(){
    try{
      const r = await fetch('/api/admin/payments/summary',{credentials:'include'});
      const s = await r.json();
      document.getElementById('totalRevenue').textContent = fmtCurrency(s.totalRevenue||0);
      document.getElementById('totalPayments').textContent = (s.totalPayments||0).toString();
      document.getElementById('lastPayment').textContent = s.lastPaymentDate? fmtDate(s.lastPaymentDate): '-';
      const byStatus = document.getElementById('byStatus');
      if(byStatus){
        byStatus.innerHTML = '';
        (s.byStatus||[]).forEach(row=>{
          const div = document.createElement('div');
          div.className='method-item';
          div.innerHTML = `<span>${row.status}</span><strong>${row.count}</strong>`;
          byStatus.appendChild(div);
        });
      }
      const byMethod = document.getElementById('byMethod');
      if(byMethod){
        byMethod.innerHTML='';
        (s.byMethod||[]).forEach(row=>{
          const div = document.createElement('div');
          div.className='method-item';
          div.innerHTML = `<span>${row.method}</span><strong>${fmtCurrency(row.amount)} (${row.count})</strong>`;
          byMethod.appendChild(div);
        });
      }
    }catch(err){ console.error('Summary load error',err); }
  }

  async function fetchRows(){
    const r = await fetch('/api/admin/payments',{credentials:'include'});
    return r.json();
  }

  function toRowArray(p){
    return [
      p.paymentId,
      fmtDate(p.paymentDate),
      `#${p.bookingId}`,
      (p.customerName||'-'),
      (p.customerEmail||'-'),
      (p.paymentMethod||'-'),
      fmtCurrency(p.amount),
      badge(p.paymentStatus),
      actionButtons(p)
    ];
  }

  async function loadTable(){
    try{
      const rows = await fetchRows();
      if(!$.fn.DataTable.isDataTable('#paymentsTable')){
        dt = $('#paymentsTable').DataTable({
          data: rows.map(toRowArray),
          order:[[1,'desc']],
          pageLength: 10,
          lengthMenu: [10,25,50,100],
          autoWidth: false
        });
      } else {
        dt = $('#paymentsTable').DataTable();
        dt.clear();
        rows.forEach(p=> dt.row.add(toRowArray(p)));
        dt.draw(false);
      }
    }catch(err){ console.error('Payments load error',err); }
  }

  async function doConfirm(id){
    const r = await fetch(`/api/admin/payments/${id}/confirm`,{method:'POST',credentials:'include'});
    const j = await r.json();
    if(!r.ok || !j.success) throw new Error(j.message||'Confirm failed');
    return j;
  }
  async function doCancel(id){
    const r = await fetch(`/api/admin/payments/${id}/cancel`,{method:'POST',credentials:'include'});
    const j = await r.json();
    if(!r.ok || !j.success) throw new Error(j.message||'Cancel failed');
    return j;
  }
  async function doDelete(id){
    const r = await fetch(`/api/admin/payments/${id}`,{method:'DELETE',credentials:'include'});
    const j = await r.json().catch(()=>({success:r.ok}));
    if(!r.ok || (j && j.success===false)) throw new Error((j && j.message)||'Delete failed');
    return j;
  }

  function inlineUpdateAfterConfirm(btn){
    const $row = $(btn).closest('tr');
    const row = dt.row($row);
    const data = row.data();
    // data indexes after removing Txn: status idx=7, actions idx=8
    data[7] = badge('Completed');
    const id = $(btn).data('id');
    data[8] = `<button class="btn btn-sm btn-danger js-cancel" data-id="${id}"><i class="fa fa-times"></i> Cancel</button> `+
              `<button class="btn btn-sm btn-outline-danger js-delete" data-id="${id}"><i class="fa fa-trash"></i> Delete</button>`;
    row.data(data).draw(false);
  }

  function inlineUpdateAfterCancel(btn){
    const $row = $(btn).closest('tr');
    const row = dt.row($row);
    const data = row.data();
    data[7] = badge('Cancelled');
    const id = $(btn).data('id');
    data[8] = `<button class="btn btn-sm btn-outline-danger js-delete" data-id="${id}"><i class="fa fa-trash"></i> Delete</button>`;
    row.data(data).draw(false);
  }

  function inlineRemoveRow(btn){
    const $row = $(btn).closest('tr');
    dt.row($row).remove().draw(false);
  }

  function wireActionsOnce(){
    if(actionsWired) return; actionsWired = true;
    document.addEventListener('click', async (e)=>{
      const confirmBtn = e.target.closest && e.target.closest('.js-confirm');
      const cancelBtn = e.target.closest && e.target.closest('.js-cancel');
      const deleteBtn = e.target.closest && e.target.closest('.js-delete');
      const btn = confirmBtn||cancelBtn||deleteBtn;
      if(!btn) return;
      const id = btn.getAttribute('data-id');
      try{
        btn.disabled = true;
        if(confirmBtn){
          await doConfirm(id);
          inlineUpdateAfterConfirm(btn);
          loadSummary();
        } else if(cancelBtn){
          await doCancel(id);
          inlineUpdateAfterCancel(btn);
          loadSummary();
        } else if(deleteBtn){
          if(!window.confirm('Delete this payment permanently? This cannot be undone.')){ btn.disabled=false; return; }
          await doDelete(id);
          inlineRemoveRow(btn);
          loadSummary();
        }
      }catch(err){
        console.error('Action error',err);
        alert(err.message||'Action failed');
      } finally {
        btn.disabled = false;
      }
    }, true);
  }

  document.addEventListener('DOMContentLoaded', function(){
    loadSummary();
    loadTable();
    wireActionsOnce();
    const refreshBtn = document.getElementById('refreshBtn');
    if(refreshBtn) refreshBtn.addEventListener('click', ()=>{ loadSummary(); loadTable(); });

    const exportBtn = document.getElementById('exportBtn');
    if(exportBtn){
      exportBtn.addEventListener('click', ()=>{
        // Trigger file download via navigation to endpoint
        window.location.href = '/api/admin/payments/export';
      });
    }

    const printBtn = document.getElementById('printBtn');
    if(printBtn){
      printBtn.addEventListener('click', ()=>{ window.print(); });
    }
  });
})();
