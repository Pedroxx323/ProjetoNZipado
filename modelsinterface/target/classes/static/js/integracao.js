const el = id => document.getElementById(id);
const val = id => el(id).value;
const money = n => Number(n || 0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'});
async function api(path,method='GET',body){
 const r=await fetch('/api'+path,{method,headers:{'Content-Type':'application/json'},...(body===undefined?{}:{body:JSON.stringify(body)})});
 const text=await r.text(); if(!r.ok) throw Error(text || 'Erro HTTP '+r.status); return text ? JSON.parse(text):null;
}
function error(e){el('mensagem').textContent=e.message;}
function ok(s){el('mensagem').textContent=s;}
function guard(fn){return async (...args)=>{try{await fn(...args)}catch(e){error(e)}};}
function options(id,items,key,label){const select=el(id);select.replaceChildren(new Option('Selecione','')); for(const x of items)select.add(new Option(label(x),x[key]));}
function row(tbody,values,action){const tr=tbody.insertRow(); for(const value of values)tr.insertCell().textContent=value??''; if(action){const td=tr.insertCell();td.append(action);}}
function button(label,fn){const b=document.createElement('button');b.type='button';b.textContent=label;b.onclick=guard(fn);return b;}
function submit(fn){el('formulario').addEventListener('submit',guard(async e=>{e.preventDefault();const b=e.submitter;b.disabled=true;try{await fn();}finally{b.disabled=false;}}));}
const uid=()=>crypto.randomUUID();
