/* ============================================================
   dashboard.js — Carregamento do painel principal
   ============================================================ */

async function loadDashboard() {
  try {
    const [cli, mat, orc] = await Promise.all([
      api('GET', '/cliente').catch(() => []),
      api('GET', '/material').catch(() => []),
      api('GET', '/orcamento').catch(() => []),
    ]);

    cacheClientes   = cli || [];
    cacheMateriais  = mat || [];
    cacheOrcamentos = orc || [];

    document.getElementById('d-clientes').innerText = cacheClientes.length;
    document.getElementById('d-mat').innerText      = cacheMateriais.length;
    document.getElementById('d-orc').innerText      = cacheOrcamentos.length;

    const fat = cacheOrcamentos.reduce((acc, o) => acc + (o.valorTotal || 0), 0);
    document.getElementById('d-fat').innerText = fmt(fat);

    // Tabela de últimos orçamentos
    // CORRIGIDO: campo 'id' em vez de 'idOrcamento' (backend usa @JsonProperty("id"))
    // CORRIGIDO: campo 'dataCriacao' em vez de 'data'
    const tbOrc  = document.getElementById('dash-orc-tbody');
    const ultOrc = [...cacheOrcamentos].reverse().slice(0, 5);
    tbOrc.innerHTML = ultOrc.length
      ? ultOrc.map(o => `
          <tr>
            <td><strong>#${o.id}</strong></td>
            <td>${o.cliente?.nome || '—'}</td>
            <td>${fmtData(o.dataCriacao)}</td>
            <td><span class="tag status-${o.status}">${o.status}</span></td>
            <td>${fmt(o.valorTotal)}</td>
          </tr>`).join('')
      : `<tr><td colspan="5" class="empty-state">Nenhum orçamento recente</td></tr>`;

    // Tabela de materiais recentes
    // CORRIGIDO: campo 'precoMetro' em vez de 'precoMetroQuadrado'
    const tbMat  = document.getElementById('dash-mat-tbody');
    const ultMat = [...cacheMateriais].reverse().slice(0, 5);
    tbMat.innerHTML = ultMat.length
      ? ultMat.map(m => `
          <tr>
            <td>${m.nome}</td>
            <td>${fmt(m.precoMetro)}</td>
          </tr>`).join('')
      : `<tr><td colspan="2" class="empty-state">Nenhum material recente</td></tr>`;

  } catch (e) {
    console.error('Erro ao carregar dashboard', e);
  }
}