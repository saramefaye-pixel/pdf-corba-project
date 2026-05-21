const API = "https://spring-pdf-client.onrender.com/api/pdf";

// ========== UTILITAIRES ==========

function showResult(id, message, type) {
    const el = document.getElementById(id);
    el.className = 'result ' + type;
    el.innerHTML = message;
}

function downloadBlob(blob, filename, type) {
    const url = URL.createObjectURL(new Blob([blob], { type: type }));
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' o';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' Ko';
    return (bytes / (1024 * 1024)).toFixed(1) + ' Mo';
}

function showFileInfo(inputId, resultId) {
    const input = document.getElementById(inputId);
    input.addEventListener('change', function() {
        const files = this.files;
        if (!files || files.length === 0) return;
        if (files.length === 1) {
            const f = files[0];
            showResult(resultId,
                'Fichier sélectionné : <strong>' + f.name + '</strong> — ' + formatSize(f.size),
                'info');
        } else {
            let info = files.length + ' fichiers sélectionnés : ';
            let total = 0;
            for (let f of files) total += f.size;
            info += formatSize(total) + ' au total';
            showResult(resultId, info, 'info');
        }
    });
}

// Initialiser les aperçus de fichiers
document.addEventListener('DOMContentLoaded', function() {
    showFileInfo('mergeFiles', 'mergeResult');
    showFileInfo('splitFile', 'splitResult');
    showFileInfo('extractFile', 'extractResult');
    showFileInfo('deleteFile', 'deleteResult');
    showFileInfo('passFile', 'passResult');
    showFileInfo('imgFile', 'imgResult');
    showFileInfo('textFile', 'textResult');

    // Drag & drop sur toutes les zones fichier
    document.querySelectorAll('input[type="file"]').forEach(input => {
        const field = input.closest('.field');
        if (!field) return;

        field.addEventListener('dragover', e => {
            e.preventDefault();
            field.style.background = 'rgba(107,26,26,0.06)';
            field.style.borderRadius = '10px';
        });

        field.addEventListener('dragleave', e => {
            field.style.background = '';
        });

        field.addEventListener('drop', e => {
            e.preventDefault();
            field.style.background = '';
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                input.files = files;
                input.dispatchEvent(new Event('change'));
            }
        });
    });
});

// ========== FONCTIONS PDF ==========

async function mergePDFs() {
    const files = document.getElementById('mergeFiles').files;
    if (files.length < 2) return showResult('mergeResult',
        'Sélectionnez au moins 2 fichiers PDF.', 'error');
    const form = new FormData();
    for (let f of files) form.append('files', f);
    try {
        showResult('mergeResult', 'Fusion en cours, veuillez patienter...', 'info');
        const res = await fetch(`${API}/merge`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'fusion.pdf', 'application/pdf');
        showResult('mergeResult',
            'Fusion réussie. ' + files.length + ' fichiers combinés. Téléchargement effectué.', 'success');
    } catch(e) {
        showResult('mergeResult', 'Erreur : ' + e.message, 'error');
    }
}

async function splitPDF() {
    const file = document.getElementById('splitFile').files[0];
    if (!file) return showResult('splitResult',
        'Sélectionnez un fichier PDF.', 'error');
    const form = new FormData();
    form.append('file', file);
    try {
        showResult('splitResult', 'Découpage en cours...', 'info');
        const res = await fetch(`${API}/split`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'pages_decoupees.zip', 'application/zip');
        showResult('splitResult',
            'Découpage réussi. Les pages ont été téléchargées dans un fichier ZIP.', 'success');
    } catch(e) {
        showResult('splitResult', 'Erreur : ' + e.message, 'error');
    }
}

async function extractPages() {
    const file = document.getElementById('extractFile').files[0];
    const pages = document.getElementById('extractPages').value.trim();
    if (!file) return showResult('extractResult',
        'Sélectionnez un fichier PDF.', 'error');
    if (!pages) return showResult('extractResult',
        'Indiquez les numéros de pages (exemple : 1, 3, 5).', 'error');
    const invalid = pages.split(',').some(p => isNaN(parseInt(p.trim())) || parseInt(p.trim()) < 1);
    if (invalid) return showResult('extractResult',
        'Numéros de pages invalides. Utilisez des entiers séparés par des virgules.', 'error');
    const form = new FormData();
    form.append('file', file);
    form.append('pages', pages);
    try {
        showResult('extractResult', 'Extraction en cours...', 'info');
        const res = await fetch(`${API}/extract-pages`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'extrait.pdf', 'application/pdf');
        showResult('extractResult',
            'Pages extraites avec succès. Le fichier a été téléchargé.', 'success');
    } catch(e) {
        showResult('extractResult', 'Erreur : ' + e.message, 'error');
    }
}

async function deletePage() {
    const file = document.getElementById('deleteFile').files[0];
    const index = document.getElementById('deleteIndex').value;
    if (!file) return showResult('deleteResult',
        'Sélectionnez un fichier PDF.', 'error');
    if (index === '') return showResult('deleteResult',
        'Indiquez le numéro de page à supprimer.', 'error');
    if (parseInt(index) < 0) return showResult('deleteResult',
        'Le numéro de page doit être supérieur ou égal à 0.', 'error');
    const form = new FormData();
    form.append('file', file);
    form.append('pageIndex', parseInt(index));
    try {
        showResult('deleteResult', 'Suppression en cours...', 'info');
        const res = await fetch(`${API}/delete-page`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'modifie.pdf', 'application/pdf');
        showResult('deleteResult',
            'Page ' + index + ' supprimée. Le fichier modifié a été téléchargé.', 'success');
    } catch(e) {
        showResult('deleteResult', 'Erreur : ' + e.message, 'error');
    }
}

async function addPassword() {
    const file = document.getElementById('passFile').files[0];
    const pass = document.getElementById('password').value.trim();
    if (!file) return showResult('passResult',
        'Sélectionnez un fichier PDF.', 'error');
    if (!pass) return showResult('passResult',
        'Saisissez un mot de passe.', 'error');
    if (pass.length < 4) return showResult('passResult',
        'Mot de passe trop court. Minimum 4 caractères.', 'error');
    const form = new FormData();
    form.append('file', file);
    form.append('password', pass);
    try {
        showResult('passResult', 'Protection en cours...', 'info');
        const res = await fetch(`${API}/add-password`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'protege.pdf', 'application/pdf');
        showResult('passResult',
            'Document protégé avec succès. Le fichier a été téléchargé.', 'success');
    } catch(e) {
        showResult('passResult', 'Erreur : ' + e.message, 'error');
    }
}

async function toImages() {
    const file = document.getElementById('imgFile').files[0];
    if (!file) return showResult('imgResult',
        'Sélectionnez un fichier PDF.', 'error');
    const form = new FormData();
    form.append('file', file);
    try {
        showResult('imgResult',
            'Conversion en cours. Cette opération peut prendre quelques secondes...', 'info');
        const res = await fetch(`${API}/to-images`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'images_pdf.zip', 'application/zip');
        showResult('imgResult',
            'Conversion réussie. Toutes les pages ont été exportées en PNG dans un fichier ZIP.', 'success');
    } catch(e) {
        showResult('imgResult', 'Erreur : ' + e.message, 'error');
    }
}

async function extractText() {
    const file = document.getElementById('textFile').files[0];
    if (!file) return showResult('textResult',
        'Sélectionnez un fichier PDF.', 'error');
    const form = new FormData();
    form.append('file', file);
    try {
        showResult('textResult', 'Extraction du texte en cours...', 'info');
        const res = await fetch(`${API}/extract-text`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const text = await res.text();
        if (!text || text.trim() === '') {
            showResult('textResult',
                'Aucun texte extractible trouvé dans ce document.', 'error');
        } else {
            showResult('textResult', text, 'text-result');
        }
    } catch(e) {
        showResult('textResult', 'Erreur : ' + e.message, 'error');
    }
}

async function createPDF() {
    const content = document.getElementById('createContent').value.trim();
    if (!content) return showResult('createResult',
        'Saisissez du contenu pour créer le document.', 'error');
    if (content.length < 3) return showResult('createResult',
        'Contenu trop court.', 'error');
    const form = new FormData();
    form.append('content', content);
    try {
        showResult('createResult', 'Génération du document en cours...', 'info');
        const res = await fetch(`${API}/create`, { method: 'POST', body: form });
        if (!res.ok) throw new Error('Erreur serveur ' + res.status);
        const blob = await res.blob();
        downloadBlob(blob, 'nouveau.pdf', 'application/pdf');
        showResult('createResult',
            'Document PDF généré et téléchargé avec succès.', 'success');
    } catch(e) {
        showResult('createResult', 'Erreur : ' + e.message, 'error');
    }
}
