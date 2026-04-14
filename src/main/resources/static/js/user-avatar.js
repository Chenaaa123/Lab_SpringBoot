/**
 * @fileoverview
 * @description
 * @version 1.0.0
 * @author
 */

// API Base URL
const API_BASE_URL = '/lab';

// Current user info storage
let currentUser = null;

/**
 * @description
 * @returns {Promise<Object>}
 */
async function getCurrentUser() {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('No authentication token found');
            return null;
        }

        const response = await fetch(`${API_BASE_URL}/auth/current`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.code === 200) {
            currentUser = result.data;
            return currentUser;
        } else {
            console.error('Failed to get current user:', result.message);
            return null;
        }
    } catch (error) {
        console.error('Error getting current user:', error);
        return null;
    }
}

/**
 * @description
 * @param {string} avatarUrl
 * @returns {string}
 */
function getAvatarDisplayUrl(avatarUrl) {
    if (!avatarUrl) {
        return '/static/images/default-avatar.png';
    }
    
    if (avatarUrl.startsWith('data:image/')) {
        return avatarUrl;
    }
    
    if (avatarUrl.startsWith('http')) {
        return avatarUrl;
    }
    
    return `/static/images/avatars/${avatarUrl}`;
}

/**
 * @description
 * @param {Object} user
 */
function renderUserAvatar(user) {
    const avatarContainer = document.getElementById('user-avatar-container');
    if (!avatarContainer) {
        console.error('User avatar container not found');
        return;
    }

    const avatarUrl = getAvatarDisplayUrl(user.avatar);
    
    avatarContainer.innerHTML = `
        <div class="user-avatar-wrapper">
            <img src="${avatarUrl}" 
                 alt="${user.userName || 'User'}" 
                 class="user-avatar"
                 onerror="this.src='/static/images/default-avatar.png'">
            <div class="user-info">
                <span class="user-name">${user.userName || user.userAccount || 'Unknown'}</span>
                <span class="user-role">${user.role || 'User'}</span>
            </div>
        </div>
    `;
}

/**
 * @description
 */
async function initUserAvatar() {
    const user = await getCurrentUser();
    if (user) {
        renderUserAvatar(user);
    }
}

/**
 * @description
 * @param {string} newAvatarUrl
 * @returns {Promise<boolean>}
 */
async function updateUserAvatar(newAvatarUrl) {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('No authentication token found');
            return false;
        }

        const response = await fetch(`${API_BASE_URL}/users/${currentUser.userId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                avatar: newAvatarUrl
            })
        });

        const result = await response.json();
        if (result.code === 200) {
            currentUser = result.data;
            renderUserAvatar(currentUser);
            return true;
        } else {
            console.error('Failed to update avatar:', result.message);
            return false;
        }
    } catch (error) {
        console.error('Error updating avatar:', error);
        return false;
    }
}

/**
 * @description
 */
function setupAvatarUpload() {
    const uploadInput = document.getElementById('avatar-upload-input');
    const uploadButton = document.getElementById('avatar-upload-button');
    
    if (!uploadInput || !uploadButton) {
        return;
    }

    uploadButton.addEventListener('click', () => {
        uploadInput.click();
    });

    uploadInput.addEventListener('change', async (event) => {
        const file = event.target.files[0];
        if (!file) {
            return;
        }

        if (!file.type.startsWith('image/')) {
            alert('Please select an image file');
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            alert('Image size should be less than 5MB');
            return;
        }

        try {
            const base64 = await fileToBase64(file);
            const success = await updateUserAvatar(base64);
            if (success) {
                alert('Avatar updated successfully!');
            } else {
                alert('Failed to update avatar');
            }
        } catch (error) {
            console.error('Error processing avatar:', error);
            alert('Error processing avatar');
        }
    });
}

/**
 * @description
 * @param {File} file
 * @returns {Promise<string>}
 */
function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

/**
 * @description
 */
function setupAvatarDropdown() {
    const avatarContainer = document.getElementById('user-avatar-container');
    if (!avatarContainer) {
        return;
    }

    avatarContainer.addEventListener('click', () => {
        const dropdown = document.getElementById('user-dropdown');
        if (dropdown) {
            dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
        }
    });

    document.addEventListener('click', (event) => {
        if (!avatarContainer.contains(event.target)) {
            const dropdown = document.getElementById('user-dropdown');
            if (dropdown) {
                dropdown.style.display = 'none';
            }
        }
    });
}

/**
 * @description
 */
document.addEventListener('DOMContentLoaded', () => {
    initUserAvatar();
    setupAvatarUpload();
    setupAvatarDropdown();
});

/**
 * @description
 */
window.addEventListener('storage', (event) => {
    if (event.key === 'token') {
        initUserAvatar();
    }
});

/**
 * @description
 */
window.updateUserAvatar = updateUserAvatar;
window.getCurrentUser = getCurrentUser;
