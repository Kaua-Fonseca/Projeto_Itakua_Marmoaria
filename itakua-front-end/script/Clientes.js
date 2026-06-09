/* ============================================================
   clientes.js — CRUD de clientes
   ============================================================ */

async function loadClientes() {
  try {
    cacheClientes = await api('GET', '/cliente') || [];
    filterClientes();
    loadClientesCombo();
  } catch (e) {
    console.error('Erro ao carregar clientes', e);
  }
}

function filterClientes() {
  const q    = document.getElementById('search-cli').value.toLowerCase();
  const list = cacheClientes.filter(c => c.nome.toLowerCase().includes(q));
  const tb   = document.getElementById('tbody-cli');

  if (!list.length) {
    tb.innerHTML = `
      <tr><td colspan="5">
        <div class="empty-state">
          <div class="empty-icon">👤</div>
          <div class="empty-text">Nenhum cliente encontrado</div>
        </div>
      </td></tr>`;
    return;
  }

  tb.innerHTML = list.map(c => `
    <tr>
      <td>${c.id}</td>
      <td><strong>${c.nome}</strong></td>
      <td>${c.telefone || '—'}</td>
      <td>${c.endereco || '—'}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="editCliente(${c.id})">✎ Editar</button>
        <button class="btn btn-ghost-red btn-sm" onclick="delCliente(${c.id})">✕ Excluir</button>
      </td>
    </tr>`).join('');
}

function openModalCliente() {
  document.getElementById('cli-id').value    = '';
  document.getElementById('cli-nome').value  = '';
  document.getElementById('cli-end').value   = '';
  document.getElementById('cli-tel').value   = '';
  document.getElementById('m-cli-title').innerText  = 'Novo Cliente';
  document.getElementById('alert-cli').innerHTML    = '';
  openModal('m-cliente');
}

function editCliente(id) {
  const c = cacheClientes.find(x => x.id === id);
  if (!c) return;
  document.getElementById('cli-id').value    = c.id;
  document.getElementById('cli-nome').value  = c.nome;
  document.getElementById('cli-end').value   = c.endereco || '';
  document.getElementById('cli-tel').value   = c.telefone || '';
  document.getElementById('m-cli-title').innerText  = 'Editar Cliente';
  document.getElementById('alert-cli').innerHTML    = '';
  openModal('m-cliente');
}

async function salvarCliente() {
  const id   = document.getElementById('cli-id').value;
  const body = {
    nome:     document.getElementById('cli-nome').value.trim(),
    endereco: document.getElementById('cli-end').value.trim(),
    telefone: document.getElementById('cli-tel').value.trim(),
  };

  if (!body.nome) return showAlert('alert-cli', 'O nome é obrigatório!', 'err');

  setBusy('btn-save-cli', true);
  try {
    if (id) await api('PUT', `/cliente/${id}`, body);
    else    await api('POST', '/cliente', body);
    closeModal('m-cliente');
    loadClientes();
    if (document.getElementById('tab-dashboard').classList.contains('active')) loadDashboard();
  } catch (e) {
    showAlert('alert-cli', 'Erro: ' + e.message, 'err');
  } finally {
    setBusy('btn-save-cli', false);
  }
}

async function delCliente(id) {
  if (!confirm('Deseja realmente excluir este cliente?')) return;
  try {
    await api('DELETE', `/cliente/${id}`);
    loadClientes();
    if (document.getElementById('tab-dashboard').classList.contains('active')) loadDashboard();
  } catch (e) {
    alert('Erro ao excluir: ' + e.message);
  }
}

/** Popula o select de clientes no formulário de orçamento. */
function loadClientesCombo() {
  const sel = document.getElementById('orc-cliente');
  if (!sel) return;
  sel.innerHTML = '<option value="">Selecione o Cliente...</option>' +
    cacheClientes.map(c => `<option value="${c.id}">${c.nome}</option>`).join('');
}