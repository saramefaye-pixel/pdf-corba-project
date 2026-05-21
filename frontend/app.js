const API = "https://spring-pdf-client.onrender.com/api/pdf";

function showResult(id, message, type) {
    const el = document.getElementById(id);
    el.className = 'result ' + type;
    el.innerHTML = message;
}

function downloadBlob(blob, filename, type) {
    const url = URL.createObjectURL(new Blob([blob], {type: type}));
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}

async function mergePDFs() {
    const files = document.getElementById('mergeFiles').files;
    if (files.length < 2) return showResult('mergeResult', 'Sélectionnez au moins 2 fichiers PDF.', 'error');
    const form = new FormData();
    for (let f of files) form.append('files', f);
    try {
        showResult('mergeResult', 'Fusion en cours, veuillez patienter...', 'info');
        const res = await fetch(`${API}/merge`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'fusion.pdf', 'application/pdf');
        showResult('mergeResult', 'Fusion réussie. Le fichier a été téléchargé.', 'success');
    } catch(e) { showResult('mergeResult', 'Erreur : ' + e.message, 'error'); }
}

async function splitPDF() {
    const file = document.getElementById('splitFile').files[0];
    if (!file) return showResult('splitResult', 'Sélectionnez un fichier PDF.', 'error');
    const form = new FormData();
    form.append('file', file);
    try {
        showResult('splitResult', 'Découpage en cours...', 'info');
        const res = await fetch(`${API}/split`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const data = await res.json();
        showResult('splitResult', 'PDF découpé en ' + data.pages + ' page(s) avec succès.', 'success');
    } catch(e) { showResult('splitResult', 'Erreur : ' + e.message, 'error'); }
}

async function extractPages() {
    const file = document.getElementById('extractFile').files[0];
    const pages = document.getElementById('extractPages').value.trim();
    if (!file) return showResult('extractResult', 'Sélectionnez un fichier PDF.', 'error');
    if (!pages) return showResult('extractResult', 'Indiquez les numéros de pages (ex: 1,3,5).', 'error');
    const form = new FormData();
    form.append('file', file);
    form.append('pages', pages);
    try {
        showResult('extractResult', 'Extraction en cours...', 'info');
        const res = await fetch(`${API}/extract-pages`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'extrait.pdf', 'application/pdf');
        showResult('extractResult', 'Pages extraites. Le fichier a été téléchargé.', 'success');
    } catch(e) { showResult('extractResult', 'Erreur : ' + e.message, 'error'); }
}

async function deletePage() {
    const file = document.getElementById('deleteFile').files[0];
    const index = document.getElementById('deleteIndex').value;
    if (!file) return showResult('deleteResult', 'Sélectionnez un fichier PDF.', 'error');
    if (index === '') return showResult('deleteResult', 'Indiquez le numéro de page à supprimer.', 'error');
    const form = new FormData();
    form.append('file', file);
    form.append('pageIndex', parseInt(index));
    try {
        showResult('deleteResult', 'Suppression en cours...', 'info');
        const res = await fetch(`${API}/delete-page`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'modifie.pdf', 'application/pdf');
        showResult('deleteResult', 'Page supprimée. Le fichier modifié a été téléchargé.', 'success');
    } catch(e) { showResult('deleteResult', 'Erreur : ' + e.message, 'error'); }
}

async function addPassword() {
    const file = document.getElementById('passFile').files[0];
    const pass = document.getElementById('password').value.trim();
    if (!file) return showResult('passResult', 'Sélectionnez un fichier PDF.', 'error');
    if (!pass) return showResult('passResult', 'Saisissez un mot de passe.', 'error');
    if (pass.length < 4) return showResult('passResult', 'Mot de passe trop court (minimum 4 caractères).', 'error');
    const form = new FormData();
    form.append('file', file);
    form.append('password', pass);
    try {
        showResult('passResult', 'Protection en cours...', 'info');
        const res = await fetch(`${API}/add-password`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'protege.pdf', 'application/pdf');
        showResult('passResult', 'PDF protégé. Le fichier a été téléchargé.', 'success');
    } catch(e) { showResult('passResult', 'Erreur : ' + e.message, 'error'); }
}

async function toImages() {
    const file = document.getElementById('imgFile').files[0];
    if (!file) return showResult('imgResult', 'Sélectionnez un fichier PDF.', 'error');
    const form = new FormData();
    form.append('file', file);
    try {
        showResult('imgResult', 'Conversion en cours, veuillez patienter...', 'info');
        const res = await fetch(`${API}/to-images`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'page_1.png', 'image/png');
        showResult('imgResult', 'Conversion réussie. L\'image a été téléchargée.', 'success');
    } catch(e) { showResult('imgResult', 'Erreur : ' + e.message, 'error'); }
}

async function extractText() {
    const file = document.getElementById('textFile').files[0];
    if (!file) return showResult('textResult', 'Sélectionnez un fichier PDF.', 'error');
    const form = new FormData();
    form.append('file', file);
    try {
        showResult('textResult', 'Extraction du texte en cours...', 'info');
        const res = await fetch(`${API}/extract-text`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const text = await res.text();
        if (!text || text.trim() === '') {
            showResult('textResult', 'Aucun texte trouvé dans ce document.', 'error');
        } else {
            showResult('textResult', text, 'text-result');
        }
    } catch(e) { showResult('textResult', 'Erreur : ' + e.message, 'error'); }
}

async function createPDF() {
    const content = document.getElementById('createContent').value.trim();
    if (!content) return showResult('createResult', 'Saisissez du contenu pour créer le PDF.', 'error');
    const form = new FormData();
    form.append('content', content);
    try {
        showResult('createResult', 'Création du PDF en cours...', 'info');
        const res = await fetch(`${API}/create`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'nouveau.pdf', 'application/pdf');
        showResult('createResult', 'PDF créé et téléchargé avec succès.', 'success');
    } catch(e) { showResult('createResult', 'Erreur : ' + e.message, 'error'); }
}
