import {api, money, monthSummary, filterRows} from './finance.js';
const $ = id => document.getElementById(id);
const today = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; };
let transactions = [], deleting = null;
const colors = ['#687d3b','#a1ab8c','#c1937e','#264932','#7f9389','#b7a572','#758359','#acb4a3'];
$('mes').value = today().slice(0,7);
function report(error) { if(error.status === 401) location.replace('login.html'); else $('status').textContent = error.message; }
function element(tag,text,className) { const e=document.createElement(tag); e.textContent=text; if(className)e.className=className; return e; }
function render() {
    const summary = monthSummary(transactions, $('mes').value);
    $('saldo').textContent=money(summary.balance); $('receitas').textContent=money(summary.income); $('despesas').textContent=money(summary.expense); $('total-grafico').textContent=money(summary.expense);
    $('categorias-resumo').replaceChildren();
    let offset=0;
    const stops=summary.categories.map((c,i) => {
        const li=document.createElement('li'), title=element('span', ''), dot=element('i','','dot'); dot.style.background=colors[i%colors.length];
        title.append(dot,document.createTextNode(c.name),element('small',`${c.percent.toFixed(1).replace('.',',')}% dos gastos`));
        li.append(title,element('strong',money(c.value))); $('categorias-resumo').append(li);
        const start=offset; offset+=c.percent; return `${colors[i%colors.length]} ${start}% ${offset}%`;
    });
    $('grafico').style.background=stops.length ? `conic-gradient(${stops.join(',')})` : '#e5e7dc';
    $('analise-vazia').hidden=summary.categories.length>0; $('analise-vazia').textContent='Sem despesas neste mês. Seu resumo aparecerá aqui.';
    $('insight').textContent=summary.categories.length ? `${summary.categories[0].name} representa ${summary.categories[0].percent.toFixed(0)}% das despesas do mês. Acompanhar seus hábitos é o primeiro passo para planejar.` : 'Registre seus lançamentos para entender para onde vai seu dinheiro.';
    const rows=filterRows(summary.rows,$('busca').value,$('tipo-filtro').value,$('categoria-filtro').value);
    $('quantidade').textContent=`${rows.length} lançamento(s) encontrado(s)`; $('lista-vazia').hidden=rows.length>0; $('lista').replaceChildren();
    for(const t of rows) {
        const row=document.createElement('tr'), title=element('td',t.descricao); title.append(element('small',t.categoria.nome));
        const actions=element('td','');
        for(const [label,fn] of [['Editar',()=>openEditor(t)],['Excluir',()=>confirmDelete(t)]]) {
            const b=element('button',label,'ghost'); b.type='button'; b.setAttribute('aria-label',`${label} ${t.descricao}`); b.addEventListener('click',fn); actions.append(b);
        }
        row.append(title,element('td',t.data.split('-').reverse().join('/')),element('td',t.tipo==='RECEITA'?'Receita':'Despesa'),element('td',`${t.tipo==='RECEITA'?'+':'−'} ${money(t.valor)}`,`numeric ${t.tipo==='RECEITA'?'positive':'negative'}`),actions); $('lista').append(row);
    }
}
async function reload() { transactions=await api('/transactions'); render(); }
async function init() {
    $('recarregar').hidden=true; $('status').textContent='Carregando suas finanças…';
    try {
        const user=await api('/auth/me'); $('saudacao').textContent=`Olá, ${user.nome.split(' ')[0]}!`;
        const categories=await api('/categories'); $('categoria').replaceChildren(); $('categoria-filtro').length=1;
        for(const c of categories) for(const id of ['categoria','categoria-filtro']) { const o=element('option',c.nome); o.value=c.id; $(id).append(o); }
        await reload(); document.querySelectorAll('.novo').forEach(b=>b.disabled=false); $('status').textContent='';
    } catch(e) { report(e); $('recarregar').hidden=false; }
}
function openEditor(t) {
    $('form-movimento').reset(); $('form-erro').textContent=''; $('movimento-id').value=t?.id || '';
    $('editor-titulo').textContent=t?'Editar lançamento':'Novo lançamento'; $('descricao').value=t?.descricao || ''; $('valor').value=t?.valor || ''; $('data').value=t?.data || today(); $('tipo').value=t?.tipo || 'DESPESA'; if(t)$('categoria').value=t.categoria.id;
    $('editor').showModal(); $('descricao').focus();
}
document.querySelectorAll('.novo').forEach(b=>b.addEventListener('click',()=>openEditor()));
for(const id of ['fechar','cancelar']) $(id).addEventListener('click',()=>$('editor').close());
for(const id of ['mes','tipo-filtro','categoria-filtro']) $(id).addEventListener('change',render);
$('busca').addEventListener('input',render); $('recarregar').addEventListener('click',init);
$('form-movimento').addEventListener('submit',async e=>{
    e.preventDefault(); const id=$('movimento-id').value; $('salvar').disabled=true; $('form-erro').textContent='';
    const body={descricao:$('descricao').value.trim(),valor:Number($('valor').value),data:$('data').value,tipo:$('tipo').value,categoriaId:Number($('categoria').value)};
    try {
        const saved=await api('/transactions'+(id?'/'+id:''),{method:id?'PUT':'POST',body:JSON.stringify(body)});
        transactions=transactions.filter(t=>t.id!==saved.id); transactions.push(saved); transactions.sort((a,b)=>b.data.localeCompare(a.data)||b.id-a.id);
        $('mes').value=saved.data.slice(0,7); $('busca').value=''; $('tipo-filtro').value=''; $('categoria-filtro').value=''; render(); $('editor').close(); $('status').textContent='Lançamento salvo com sucesso.';
    } catch(e) { if(e.status===401)report(e); else $('form-erro').textContent=e.message; } finally { $('salvar').disabled=false; }
});
function confirmDelete(t) { deleting=t; $('excluir-descricao').textContent=`${t.descricao} · ${money(t.valor)}`; $('excluir-erro').textContent=''; $('confirmacao').showModal(); $('manter').focus(); }
$('manter').addEventListener('click',()=>$('confirmacao').close());
$('excluir').addEventListener('click',async()=>{
    $('excluir').disabled=true;
    try { await api('/transactions/'+deleting.id,{method:'DELETE'}); transactions=transactions.filter(t=>t.id!==deleting.id); render(); $('confirmacao').close(); $('status').textContent='Lançamento excluído.'; }
    catch(e) { if(e.status===401)report(e); else $('excluir-erro').textContent=e.message; }
    finally { $('excluir').disabled=false; }
});
$('sair').addEventListener('click',async()=>{try{await api('/auth/logout',{method:'POST'}); location.replace('login.html');}catch(e){report(e);}});
init();