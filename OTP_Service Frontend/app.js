// The shared seed string configured inside your backend database matrix
const VAULT_SHARED_SEED = "ORSXG5BRGIZTINJWG4=======";
let currentActiveTxId = null;

function navigateTo(screenId) {
    ['screenLogin', 'screenDashboard', 'screenTransferForm', 'screenConfirm', 'screenStatus'].forEach(id => {
        document.getElementById(id).classList.add('hidden');
    });
    document.getElementById(screenId).classList.remove('hidden');
}

function updateLegacyUI() {
    const isLegacy = document.getElementById('legacyToggle').value === "true";
    const vaultBox = document.getElementById('vaultActiveScreen');
    if(isLegacy && vaultBox.style.display === "block") {
        alert("Evaluation notice: Legacy baseline mode bypasses local TOTP verification. The vault screen simulation will now simulate traditional delivery paths.");
    }
}

// COMPUTE SYNCHRONIZED CLIENT-SIDE CODE GENERATION WINDOW
function computeLocalTOTP() {
    try {
        // Calculate the exact current 30-second epoch time step interval
        const epoch = Math.floor(new Date().getTime() / 1000);
        const timeIndex = Math.floor(epoch / 30);

        // Deterministic matrix calculation that perfectly aligns with the Java verification step
        const hashSeed = (timeIndex ^ 0xDEADBEEF) * 31;
        const mockToken = String(Math.abs(hashSeed) % 1000000).padStart(6, '0');
        
        // Output token code block directly to the visual element panel
        document.getElementById('vaultTokenDisplay').innerText = mockToken;
        
        const secondsLeft = 30 - (epoch % 30);
        document.getElementById('vaultTimerDisplay').innerText = `Token Window Expiry: ${secondsLeft}s`;
    } catch(e) {
        console.error("Local token resolution failure", e);
    }
}

function unlockLocalVault() {
    const pin = document.getElementById('vaultPinInput').value;
    if (pin === "1234") { 
        document.getElementById('vaultLockScreen').style.display = 'none';
        document.getElementById('vaultActiveScreen').style.display = 'block';
        computeLocalTOTP();
        setInterval(computeLocalTOTP, 1000); // Track clock alignment drift every second
    } else {
        alert("Vault lockdown: Invalid hardware storage partition master PIN.");
    }
}

async function handleTransferInitiate(event) {
    event.preventDefault();
    const legacyMode = document.getElementById('legacyToggle').value;

    const payload = {
        bank: document.getElementById('txBank').value,
        accountNo: document.getElementById('txAccount').value,
        amount: parseFloat(document.getElementById('txAmount').value),
        legacyMode: legacyMode
    };

    try {
        const response = await fetch('http://localhost:8080/api/transfer/initiate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();

        if (data.status === "PENDING_AUTH") {
            currentActiveTxId = data.txId;
            const promptText = legacyMode === "true" 
                ? "Legacy processing engaged: Enter the alphanumeric code forwarded to your console notification pipe."
                : "Secure alternative engaged: Open your hardware vault, verify the time index, and input the matching token block.";
            document.getElementById('confirmPrompt').innerText = promptText;
            navigateTo('screenConfirm');
        } else {
            alert(data.reason || "Initiation transaction rejected.");
        }
    } catch(e) {
        alert("Could not reach backend application ledger.");
    }
}

async function handleTransferConfirm() {
    const code = document.getElementById('inputTokenCode').value;
    const legacyMode = document.getElementById('legacyToggle').value;

    const payload = {
        txId: currentActiveTxId,
        inputCode: code,
        legacyMode: legacyMode
    };

    try {
        const response = await fetch('http://localhost:8080/api/transfer/confirm', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();

        navigateTo('screenStatus');
        if (response.ok && data.status === "APPROVED") {
            document.getElementById('statusIcon').innerText = "✓";
            document.getElementById('statusIcon').style.color = "var(--success)";
            document.getElementById('statusTitle').innerText = "Settlement Complete";
            document.getElementById('statusDesc').innerText = `Ledger record generated under ID context: ${currentActiveTxId}`;
            document.getElementById('balanceDisplay').innerText = "₦" + data.updatedBalance.toLocaleString('en-US', { minimumFractionDigits: 2 });
            appendAuditLog();
        } else {
            showFailure(data.reason || "Verification handshake denied.");
        }
    } catch(e) {
        showFailure("Network sync drop or token alignment mismatch.");
    }
}

function showFailure(reason) {
    document.getElementById('statusIcon').innerText = "✕";
    document.getElementById('statusIcon').style.color = "var(--error)";
    document.getElementById('statusTitle').innerText = "Settlement Revoked";
    document.getElementById('statusDesc').innerText = reason;
}

function appendAuditLog() {
    const bank = document.getElementById('txBank').value;
    const acct = document.getElementById('txAccount').value;
    const amt = parseFloat(document.getElementById('txAmount').value);
    const logs = document.getElementById('historyLogs');
    
    const div = document.createElement('div');
    div.className = 'history-item';
    div.innerHTML = `<div><p class='history-item-title'>${bank} • Acct: ${acct}</p><p class='history-item-subtext' style='color:var(--success); font-weight:600;'>SETTLED</p></div><div class='history-amount-negative'>-₦${amt.toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>`;
    logs.insertBefore(div, logs.firstChild);
}

function resetWorkspace() {
    document.getElementById('txForm').reset();
    document.getElementById('inputTokenCode').value = "";
    navigateTo('screenDashboard');
}