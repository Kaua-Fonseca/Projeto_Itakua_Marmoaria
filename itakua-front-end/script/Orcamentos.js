/* ============================================================
   orcamentos.js — CRUD de orçamentos, itens e cálculo local
   ============================================================ */

// Estado local do formulário de orçamento
let pingItems  = [], solItems  = [];
let pingMats   = [], solMats   = [];
let itemIdSeq  = 0;
let currentOrcId = null;

/* ---- Listagem ---- */

async function loadOrcamentos() {
  try {
    cacheOrcamentos = await api('GET', '/orcamento') || [];
    renderTabelaOrcamentos();
  } catch (e) {
    console.error('Erro ao carregar orçamentos', e);
  }
}

function renderTabelaOrcamentos() {
  const tb = document.getElementById('tbody-orc');

  if (!cacheOrcamentos.length) {
    tb.innerHTML = `
      <tr><td colspan="8">
        <div class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-text">Nenhum orçamento emitido</div>
        </div>
      </td></tr>`;
    return;
  }

  // CORRIGIDO: campo 'dataCriacao' e 'id' (com @JsonProperty no backend)
  // CORRIGIDO: campo 'prazo' em vez de 'prazoDiasUteis'
  tb.innerHTML = cacheOrcamentos.map(o => `
    <tr>
      <td><strong>#${o.id}</strong></td>
      <td>${o.cliente?.nome || '—'}</td>
      <td>${fmtData(o.dataCriacao)}</td>
      <td>${fmtArea(o.areaTotal)}</td>
      <td><strong>${fmt(o.valorTotal)}</strong></td>
      <td><span class="tag status-${o.status}">${o.status}</span></td>
      <td>${o.prazo ? o.prazo + ' dias úteis' : '—'}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="verOrcamento(${o.id})">👁 Detalhes</button>
        <button class="btn btn-ghost-red btn-sm" onclick="delOrcamento(${o.id})">✕ Excluir</button>
      </td>
    </tr>`).join('');
}

/* ---- Abertura do modal de novo orçamento ---- */

function openModalOrcamento() {
  document.getElementById('orc-id').value     = '';
  document.getElementById('orc-cliente').value = '';
  document.getElementById('orc-mat-unico').value = '';
  document.getElementById('orc-prazo').value  = '';
  document.getElementById('orc-taxa').value   = '';
  document.getElementById('orc-status').value = 'PENDENTE';
  document.getElementById('orc-obs').value    = '';

  pingItems = []; solItems = [];
  pingMats  = []; solMats  = [];
  itemIdSeq = 0;

  renderMats('ping'); renderMats('sol');
  renderItems('ping'); renderItems('sol');
  calcTotal();

  document.getElementById('m-orc-title').innerText = 'Novo Orçamento';
  document.getElementById('alert-orc').innerHTML   = '';
  openModal('m-orcamento');
}

/* ---- Gerenciamento de materiais da comparação ---- */

function addMatPing() {
  const sel = document.getElementById('orc-ping-mat-add');
  const id  = parseInt(sel.value);
  if (id && !pingMats.includes(id)) { pingMats.push(id); renderMats('ping'); }
  sel.value = '';
}

function addMatSol() {
  const sel = document.getElementById('orc-sol-mat-add');
  const id  = parseInt(sel.value);
  if (id && !solMats.includes(id)) { solMats.push(id); renderMats('sol'); }
  sel.value = '';
}

function remMat(tipo, id) {
  if (tipo === 'ping') { pingMats = pingMats.filter(x => x !== id); renderMats('ping'); }
  else                 { solMats  = solMats.filter(x => x !== id);  renderMats('sol'); }
}

function renderMats(tipo) {
  const arr = tipo === 'ping' ? pingMats : solMats;
  const div = document.getElementById(tipo === 'ping' ? 'sel-ping-mats' : 'sel-sol-mats');
  div.innerHTML = arr.map(id => {
    const m = cacheMateriais.find(x => x.id === id);
    return m
      ? `<span class="tag t-gray">${m.nome}
           <span style="cursor:pointer;margin-left:5px;color:var(--danger)"
                 onclick="remMat('${tipo}', ${id})">✕</span>
         </span>`
      : '';
  }).join('');
}

/* ---- Gerenciamento de itens (peças) ---- */

function addItem(tipo) {
  const arr = tipo === 'ping' ? pingItems : solItems;
  arr.push({ id: ++itemIdSeq, qtd: 1, compr: 0.0, larg: 0.0, tipoCorte: 'INDIVIDUAL', friso: false });
  renderItems(tipo);
  calcTotal();
}

function updateItem(tipo, id, field, val) {
  const arr  = tipo === 'ping' ? pingItems : solItems;
  const item = arr.find(x => x.id === id);
  if (!item) return;
  if      (field === 'qtd')            item.qtd   = parseInt(val) || 1;
  else if (field === 'compr' || field === 'larg') item[field] = parseFloat(val) || 0;
  else                                 item[field] = val;
  calcTotal();
}

function remItem(tipo, id) {
  if (tipo === 'ping') pingItems = pingItems.filter(x => x.id !== id);
  else                 solItems  = solItems.filter(x => x.id !== id);
  renderItems(tipo);
  calcTotal();
}

function renderItems(tipo) {
  const arr = tipo === 'ping' ? pingItems : solItems;
  const div = document.getElementById(`rows-${tipo}`);

  if (!arr.length) {
    div.innerHTML = `<div class="empty-state" style="padding:15px;font-size:12px">Nenhuma peça adicionada.</div>`;
    return;
  }

  div.innerHTML = arr.map((item, idx) => `
    <div class="item-row item-row-7">
      <input type="text" value="${idx + 1}" disabled
             style="background:#f1f5f9;text-align:center;font-weight:bold;color:#64748b;border:none">
      <input type="number" min="1" value="${item.qtd}"
             onchange="updateItem('${tipo}',${item.id},'qtd',this.value)">
      <input type="number" step="0.001" min="0" value="${item.compr}"
             onchange="updateItem('${tipo}',${item.id},'compr',this.value)" placeholder="0.000">
      <input type="number" step="0.001" min="0" value="${item.larg}"
             onchange="updateItem('${tipo}',${item.id},'larg',this.value)" placeholder="0.000">
      <select onchange="updateItem('${tipo}',${item.id},'tipoCorte',this.value)">
        <option value="INDIVIDUAL" ${item.tipoCorte === 'INDIVIDUAL' ? 'selected' : ''}>Individual</option>
        <option value="CONJUNTO"   ${item.tipoCorte === 'CONJUNTO'   ? 'selected' : ''}>Conjunto</option>
      </select>
      <select onchange="updateItem('${tipo}',${item.id},'friso',this.value==='true')">
        <option value="false" ${!item.friso ? 'selected' : ''}>Não</option>
        <option value="true"  ${ item.friso ? 'selected' : ''}>Sim</option>
      </select>
      <button class="btn-rm" onclick="remItem('${tipo}',${item.id})" title="Remover Peça">✕</button>
    </div>`).join('');
}

/* ---- Cálculo de total local (preview) ---- */

/**
 * Calcula área de um item com base nas regras de negócio (+3cm de margem).
 * Espelha a lógica do ItemOrcamento.calcularArea() no Java.
 */
function calcAreaItem(item) {
  const compCorte = (item.tipoCorte === 'CONJUNTO')
    ? item.compr * item.qtd + 0.03
    : item.compr + 0.03;
  const largCorte = item.larg + 0.03;

  return (item.tipoCorte === 'CONJUNTO')
    ? compCorte * largCorte
    : compCorte * largCorte * item.qtd;
}

function calcTotal() {
  const matUnicoId = parseInt(document.getElementById('orc-mat-unico').value);
  const taxa       = parseFloat(document.getElementById('orc-taxa').value) || 0;
  const el         = document.getElementById('orc-total-disp');

  if (!matUnicoId) {
    el.innerText = 'Selecione um material único para ver o total';
    return;
  }

  const mat      = cacheMateriais.find(m => m.id === matUnicoId);
  const todosItens = [...pingItems, ...solItems];
  const area     = todosItens.reduce((acc, i) => acc + calcAreaItem(i), 0);
  const total    = area * (mat?.precoMetro || 0) + taxa;

  el.innerText = `${fmt(total)}  (${fmtArea(area)})`;
}

/* ---- Salvar orçamento ---- */

async function salvarOrcamento() {
  const clienteId = parseInt(document.getElementById('orc-cliente').value);
  if (!clienteId) return showAlert('alert-orc', 'Selecione um cliente!', 'err');

  if (!pingItems.length && !solItems.length)
    return showAlert('alert-orc', 'Adicione ao menos uma peça (pingadeira ou soleira).', 'err');

  const matUnicoId = document.getElementById('orc-mat-unico').value;
  const taxa       = parseFloat(document.getElementById('orc-taxa').value) || 0;
  const prazo      = parseInt(document.getElementById('orc-prazo').value) || 0;

  // CORRIGIDO: payload alinhado com a entidade Java —
  //   cliente: {id}, materialUnico: {id}, prazo, listaItens com tipoItem
  const formatItem = (x, tipoItem) => ({
    comprimento:    x.compr,
    largura:        x.larg,
    quantidade:     x.qtd,
    tipoCorte:      x.tipoCorte,
    frisoCortaPingo: x.friso,
    tipoItem,
  });

  const payload = {
    cliente:       { id: clienteId },
    materialUnico: matUnicoId ? { id: parseInt(matUnicoId) } : null,
    status:        document.getElementById('orc-status').value,
    prazo,
    taxaEntrega:   taxa,
    observacoes:   document.getElementById('orc-obs').value.trim(),
    opcoesMateriaisPingadeira: pingMats.map(id => ({ id })),
    opcoesMateriaisSoleira:    solMats.map(id  => ({ id })),
    listaItens: [
      ...pingItems.map(x => formatItem(x, 'PINGADEIRA')),
      ...solItems.map(x  => formatItem(x, 'SOLEIRA')),
    ],
  };

  setBusy('btn-save-orc', true);
  try {
    await api('POST', '/orcamento', payload);
    closeModal('m-orcamento');
    loadOrcamentos();
    if (document.getElementById('tab-dashboard').classList.contains('active')) loadDashboard();
  } catch (e) {
    showAlert('alert-orc', 'Erro: ' + e.message, 'err');
  } finally {
    setBusy('btn-save-orc', false);
  }
}

async function delOrcamento(id) {
  if (!confirm('Deseja cancelar/excluir este orçamento?')) return;
  try {
    await api('DELETE', `/orcamento/${id}`);
    loadOrcamentos();
    if (document.getElementById('tab-dashboard').classList.contains('active')) loadDashboard();
  } catch (e) {
    alert('Erro: ' + e.message);
  }
}

/* ---- Visualizar detalhes + PDF ---- */

async function verOrcamento(id) {
  currentOrcId = id;
  try {
    const o = await api('GET', `/orcamento/${id}`);
    // CORRIGIDO: campo 'dataCriacao' e 'prazo'
    document.getElementById('m-ver-title').innerText = `Orçamento #${o.id}`;
    document.getElementById('m-ver-body').innerHTML  = montarHtmlDetalhes(o);
    openModal('m-ver-orc');
  } catch (e) {
    alert('Erro ao carregar orçamento: ' + e.message);
  }
}

function montarHtmlDetalhes(o) {
  const matsPing = (o.opcoesMateriaisPingadeira || [])
    .map(m => `<li style="margin-bottom:4px;display:flex;justify-content:space-between;border-bottom:1px dashed var(--border-strong);padding-bottom:4px">
                 <span>${m.nome}</span>
                 <strong>${fmt(calcValorFinalFront(o, m))}</strong>
               </li>`).join('');

  const matsSol = (o.opcoesMateriaisSoleira || [])
    .map(m => `<li style="margin-bottom:4px;display:flex;justify-content:space-between;border-bottom:1px dashed var(--border-strong);padding-bottom:4px">
                 <span>${m.nome}</span>
                 <strong>${fmt(calcValorFinalFront(o, m))}</strong>
               </li>`).join('');

  return `
    <div class="two-col" style="margin-bottom:16px;font-size:14px">
      <div>
        <p style="margin-bottom:6px"><strong>Cliente:</strong> ${o.cliente?.nome || '—'}</p>
        <p style="margin-bottom:6px"><strong>Data:</strong> ${fmtData(o.dataCriacao)}</p>
        <p><strong>Status:</strong> <span class="tag status-${o.status}">${o.status}</span></p>
      </div>
      <div>
        <p style="margin-bottom:6px"><strong>Prazo:</strong> ${o.prazo ? o.prazo + ' dias úteis' : 'A combinar'}</p>
        <p style="margin-bottom:6px"><strong>Taxa Entrega:</strong> ${fmt(o.taxaEntrega)}</p>
        <p><strong>Área Total:</strong> ${fmtArea(o.areaTotal)}</p>
      </div>
    </div>

    ${matsPing || matsSol ? `
    <div style="background:var(--bg-body);padding:20px;border-radius:var(--radius-sm);border:1px solid var(--border-light);margin-bottom:16px">
      <h4 style="color:var(--primary);margin-bottom:12px">Opções de Material</h4>
      ${matsPing ? `<p style="font-size:12px;font-weight:600;text-transform:uppercase;color:var(--text-muted);margin-bottom:6px">Pingadeiras</p><ul style="font-size:14px;list-style:none;padding-left:0;margin-bottom:12px">${matsPing}</ul>` : ''}
      ${matsSol  ? `<p style="font-size:12px;font-weight:600;text-transform:uppercase;color:var(--text-muted);margin-bottom:6px">Soleiras</p><ul style="font-size:14px;list-style:none;padding-left:0;margin-bottom:12px">${matsSol}</ul>` : ''}
      <div style="font-size:18px;color:var(--primary);text-align:right"><strong>Total Geral: ${fmt(o.valorTotal)}</strong></div>
    </div>` : `
    <div style="font-size:18px;color:var(--primary);text-align:right;margin-bottom:16px">
      <strong>Total: ${fmt(o.valorTotal)}</strong>
    </div>`}

    <div style="font-size:13px;color:var(--text-muted)">
      <strong>Observações:</strong> ${o.observacoes || 'Nenhuma observação registrada.'}
    </div>`;
}

/**
 * Calcula valor final no lado cliente para exibição nos detalhes.
 * Espelha Orcamento.calcularValorFinal() do Java.
 */
function calcValorFinalFront(orc, material) {
  if (!material?.precoMetro) return 0;
  return (orc.areaTotal || 0) * material.precoMetro + (orc.taxaEntrega || 0);
}

function baixarPDF() {
  if (!currentOrcId) return;
  window.open(`${API}/orcamento/${currentOrcId}/pdf`, '_blank');
}