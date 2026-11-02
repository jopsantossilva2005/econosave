export const money = value => new Intl.NumberFormat('pt-BR', {style:'currency', currency:'BRL'}).format(value);
export function monthSummary(transactions, month) {
    const rows = transactions.filter(t => t.data.slice(0,7) === month);
    let income = 0, expense = 0;
    const categories = new Map();
    for (const t of rows) {
        const cents = Math.round(Number(t.valor) * 100);
        if (t.tipo === 'RECEITA') income += cents;
        else { expense += cents; categories.set(t.categoria.nome, (categories.get(t.categoria.nome) || 0) + cents); }
    }
    return {rows, income:income/100, expense:expense/100, balance:(income-expense)/100,
        categories:[...categories].sort((a,b) => b[1]-a[1]).map(([name,cents]) => ({name,value:cents/100,percent:expense ? cents/expense*100 : 0}))};
}
export function filterRows(rows, search, type, category) {
    return rows.filter(t => (!type || t.tipo === type) && (!category || String(t.categoria.id) === category)
        && t.descricao.toLocaleLowerCase('pt-BR').includes(search.trim().toLocaleLowerCase('pt-BR')));
}
export async function api(path, options = {}) {
    let response;
    try { response = await fetch('/api' + path, {...options, headers:{'Content-Type':'application/json', 'X-Requested-With':'EconoSave', ...options.headers}}); }
    catch { throw new Error('Não foi possível conectar. Confira sua conexão e tente novamente.'); }
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) { const error = new Error(data.message || 'Não foi possível concluir a solicitação.'); error.status = response.status; throw error; }
    return data;
}