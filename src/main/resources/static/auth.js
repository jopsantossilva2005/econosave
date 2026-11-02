import {api} from './finance.js';
const form=document.querySelector('form');
form.addEventListener('submit',async e=>{
    e.preventDefault(); const button=form.querySelector('button'), message=document.getElementById('mensagem'); button.disabled=true; message.textContent='';
    const body=Object.fromEntries(new FormData(form)); const register=form.dataset.action==='register';
    try { await api('/auth/'+(register?'register':'login'),{method:'POST',body:JSON.stringify(body)});
        if(register) location.href='login.html?cadastro=ok'; else location.href='index.html';
    } catch(error){ message.textContent=error.message; } finally{button.disabled=false;}
});
if(new URLSearchParams(location.search).get('cadastro')==='ok') document.getElementById('sucesso').textContent='Conta criada! Entre com seu e-mail e senha.';