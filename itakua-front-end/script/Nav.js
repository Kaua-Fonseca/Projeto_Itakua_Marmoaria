/* ============================================================
   nav.js — Navegação entre abas e inicialização da aplicação
   ============================================================ */

const TAB_TITLES = {
  dashboard:  'Dashboard',
  clientes:   'Clientes',
  materiais:  'Materiais',
  orcamentos: 'Orçamentos',
};

/**
 * Navega para uma aba e carrega os dados correspondentes.
 * @param {string} tab - Chave da aba (dashboard | clientes | materiais | orcamentos)
 */
function nav(tab) {
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  document.getElementById('tab-' + tab).classList.add('active');

  document.querySelectorAll('.nav-item').forEach(n => {
    if (n.getAttribute('onclick') === `nav('${tab}')`) n.classList.add('active');
  });

  document.getElementById('topbar-title').innerText = TAB_TITLES[tab] || tab;

  if (tab === 'dashboard')  loadDashboard();
  if (tab === 'clientes')   loadClientes();
  if (tab === 'materiais')  loadMateriais();
  if (tab === 'orcamentos') loadOrcamentos();
}

/** Verifica se a API está respondendo e atualiza o indicador no topbar. */
async function checkAPI() {
  const st = document.getElementById('api-status');
  try {
    await fetch(API + '/cliente', { method: 'GET' });
    st.style.color = 'var(--accent)';
    st.innerText = '🟢 API Online';
  } catch (_) {
    st.style.color = 'var(--danger)';
    st.innerText = '🔴 API Offline';
  }
}

/** Ponto de entrada: executado quando a página carrega. */
window.onload = async () => {
  const d = new Date();
  document.getElementById('badge-date').innerText =
    d.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });

  await checkAPI();
  loadDashboard();
  loadClientesCombo();
  loadMateriaisCombo();
};