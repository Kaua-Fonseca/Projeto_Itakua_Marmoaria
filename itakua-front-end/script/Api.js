/* ============================================================
   api.js — Configuração da URL base e cliente HTTP
   ============================================================ */

// Lê a URL do atributo data-api-url no <body> se definido,
// caso contrário usa localhost como padrão para desenvolvimento.
const API = document.body.dataset.apiUrl || 'http://localhost:8080';

/**
 * Wrapper de fetch com tratamento de erros centralizado.
 * @param {string} method  - Verbo HTTP (GET, POST, PUT, DELETE)
 * @param {string} path    - Caminho do endpoint (ex: '/cliente')
 * @param {object} [body]  - Payload JSON opcional
 * @returns {Promise<any>}
 */
async function api(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);

  const r = await fetch(API + path, opts);

  if (!r.ok) {
    let mensagem = r.statusText;
    try {
      const dados = await r.json();
      mensagem = dados.erro || dados.message || mensagem;
    } catch (_) { /* mantém statusText */ }
    throw new Error(mensagem);
  }

  if (r.status === 204) return null;
  return r.json();
}

/* ============================================================
   Funções utilitárias globais
   ============================================================ */

/** Formata valor em reais. Ex: 1234.5 → 'R$ 1.234,50' */
function fmt(v) {
  return 'R$ ' + Number(v || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/** Formata área em m². Ex: 1.5 → '1.500 m²' */
function fmtArea(v) {
  return Number(v || 0).toFixed(3) + ' m²';
}

/** Formata data ISO para pt-BR. Ex: '2024-06-01T...' → '01/06/2024' */
function fmtData(s) {
  if (!s) return '—';
  try { return new Date(s).toLocaleDateString('pt-BR'); } catch (_) { return s; }
}

/** Aplica máscara de telefone brasileiro a um input. */
function maskPhone(el) {
  let v = el.value.replace(/\D/g, '');
  if (v.length > 11) v = v.slice(0, 11);
  if (v.length > 6)      v = v.replace(/^(\d{2})(\d{5})(\d)/, '($1) $2-$3');
  else if (v.length > 2) v = v.replace(/^(\d{2})(\d)/, '($1) $2');
  else if (v.length)     v = '(' + v;
  el.value = v;
}

/**
 * Exibe um alerta temporário dentro de um elemento.
 * @param {string} containerId - ID do elemento container
 * @param {string} msg         - Mensagem
 * @param {'ok'|'err'} type    - Tipo visual
 */
function showAlert(containerId, msg, type = 'ok') {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.innerHTML = `<div class="alert alert-${type}">${msg}</div>`;
  setTimeout(() => { el.innerHTML = ''; }, 4000);
}

/**
 * Coloca um botão em estado de carregamento ou o restaura.
 * @param {string}  btnId - ID do botão
 * @param {boolean} busy  - true = spinner / false = restaura
 */
function setBusy(btnId, busy) {
  const b = document.getElementById(btnId);
  if (!b) return;
  if (busy) {
    b.dataset.orig = b.innerHTML;
    b.innerHTML = '<span class="spinner"></span>';
    b.disabled = true;
  } else {
    b.innerHTML = b.dataset.orig || b.innerHTML;
    b.disabled = false;
  }
}

/** Abre um modal pelo seu ID. */
function openModal(id) { document.getElementById(id).classList.add('open'); }

/** Fecha um modal pelo seu ID. */
function closeModal(id) { document.getElementById(id).classList.remove('open'); }