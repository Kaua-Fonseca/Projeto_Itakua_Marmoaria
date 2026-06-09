/* ============================================================
   materiais.js — CRUD de materiais
   ============================================================ */

async function loadMateriais() {
  try {
    cacheMateriais = await api('GET', '/material') || [];
    renderTabelaMateriais();
    loadMateriaisCombo();
  } catch (e) {
    console.error('Erro ao carregar materiais', e);
  }
}

function renderTabelaMateriais() {
  const tb = document.getElementById('tbody-mat');

  if (!cacheMateriais.length) {
    tb.innerHTML = `
      <tr><td colspan="5">
        <div class="empty-state">
          <div class="empty-icon">🪨</div>
          <div class="empty-text">Nenhum material cadastrado</div>
        </div>
      </td></tr>`;
    return;
  }

  // CORRIGIDO: campo 'precoMetro' em vez de 'precoMetroQuadrado'
  tb.innerHTML = cacheMateriais.map(m => `
    <tr>
      <td>${m.id}</td>
      <td><strong>${m.nome}</strong></td>
      <td>${fmt(m.precoMetro)}</td>
      <td>${m.observacao || '—'}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="editMaterial(${m.id})">✎ Editar</button>
        <button class="btn btn-ghost-red btn-sm" onclick="delMaterial(${m.id})">✕ Excluir</button>
      </td>
    </tr>`).join('');
}

function openModalMaterial() {
  document.getElementById('mat-id').value    = '';
  document.getElementById('mat-nome').value  = '';
  document.getElementById('mat-preco').value = '';
  document.getElementById('mat-obs').value   = '';
  document.getElementById('m-mat-title').innerText = 'Novo Material';
  document.getElementById('alert-mat').innerHTML   = '';
  openModal('m-material');
}

function editMaterial(id) {
  // CORRIGIDO: campo 'precoMetro' em vez de 'precoMetroQuadrado'
  const m = cacheMateriais.find(x => x.id === id);
  if (!m) return;
  document.getElementById('mat-id').value    = m.id;
  document.getElementById('mat-nome').value  = m.nome;
  document.getElementById('mat-preco').value = m.precoMetro;
  document.getElementById('mat-obs').value   = m.observacao || '';
  document.getElementById('m-mat-title').innerText = 'Editar Material';
  document.getElementById('alert-mat').innerHTML   = '';
  openModal('m-material');
}

async function salvarMaterial() {
  const id    = document.getElementById('mat-id').value;
  const preco = parseFloat(document.getElementById('mat-preco').value);
  // CORRIGIDO: enviando 'precoMetro' igual ao campo da entidade Java
  const body  = {
    nome:       document.getElementById('mat-nome').value.trim(),
    precoMetro: isNaN(preco) ? 0 : preco,
    observacao: document.getElementById('mat-obs').value.trim(),
  };

  if (!body.nome || !body.precoMetro) return showAlert('alert-mat', 'Nome e Preço são obrigatórios!', 'err');

  setBusy('btn-save-mat', true);
  try {
    if (id) await api('PUT', `/material/${id}`, body);
    else    await api('POST', '/material', body);
    closeModal('m-material');
    loadMateriais();
    if (document.getElementById('tab-dashboard').classList.contains('active')) loadDashboard();
  } catch (e) {
    showAlert('alert-mat', 'Erro: ' + e.message, 'err');
  } finally {
    setBusy('btn-save-mat', false);
  }
}

async function delMaterial(id) {
  if (!confirm('Excluir este material?')) return;
  try {
    await api('DELETE', `/material/${id}`);
    loadMateriais();
    if (document.getElementById('tab-dashboard').classList.contains('active')) loadDashboard();
  } catch (e) {
    alert('Erro: ' + e.message);
  }
}

/** Popula os selects de material no formulário de orçamento. */
function loadMateriaisCombo() {
  // CORRIGIDO: campo 'precoMetro' em vez de 'precoMetroQuadrado'
  const opts = '<option value="">(Nenhum)</option>' +
    cacheMateriais.map(m => `<option value="${m.id}">${m.nome} — ${fmt(m.precoMetro)}/m²</option>`).join('');

  const matUnico = document.getElementById('orc-mat-unico');
  if (matUnico) matUnico.innerHTML = opts;

  const optsAdd = '<option value="">+ Adicionar material...</option>' +
    cacheMateriais.map(m => `<option value="${m.id}">${m.nome}</option>`).join('');

  const ping = document.getElementById('orc-ping-mat-add');
  const sol  = document.getElementById('orc-sol-mat-add');
  if (ping) ping.innerHTML = optsAdd;
  if (sol)  sol.innerHTML  = optsAdd;
}