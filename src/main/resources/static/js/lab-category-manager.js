/**
 * @fileoverview Lab Category Management Frontend Implementation
 * @description 前端实验室分类管理功能实现
 * @version 1.0.0
 */

// API Base URL
const API_BASE_URL = '/lab';

/**
 * @description 获取所有实验室分类
 * @returns {Promise<Array>}
 */
async function getLabCategories() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/lab-categories`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('获取实验室分类失败:', error);
        throw error;
    }
}

/**
 * @description 获取实验室分类详情
 * @param {number} id 
 * @returns {Promise<Object>}
 */
async function getLabCategory(id) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/lab-categories/${id}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('获取实验室分类详情失败:', error);
        throw error;
    }
}

/**
 * @description 创建实验室分类
 * @param {Object} categoryData 
 * @returns {Promise<Object>}
 */
async function createLabCategory(categoryData) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/lab-categories`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(categoryData)
        });

        const result = await response.json();
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('创建实验室分类失败:', error);
        throw error;
    }
}

/**
 * @description 更新实验室分类
 * @param {number} id 
 * @param {Object} categoryData 
 * @returns {Promise<Object>}
 */
async function updateLabCategory(id, categoryData) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/lab-categories/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(categoryData)
        });

        const result = await response.json();
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('更新实验室分类失败:', error);
        throw error;
    }
}

/**
 * @description 删除实验室分类
 * @param {number} id 
 * @returns {Promise<boolean>}
 */
async function deleteLabCategory(id) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/lab-categories/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.code === 200) {
            return true;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('删除实验室分类失败:', error);
        throw error;
    }
}

/**
 * @description 分配实验室管理员（请求体 managerUserId，与后端一致）
 * @param {number} categoryId
 * @param {number} managerUserId
 * @returns {Promise<Object>}
 */
async function assignLabManager(categoryId, managerUserId) {
    try {
        console.log('分配管理员 - 参数:', {
            categoryId: categoryId,
            managerUserId: managerUserId
        });
        
        const token = localStorage.getItem('token');
        const requestBody = {
            managerUserId: managerUserId
        };
        
        console.log('请求体:', JSON.stringify(requestBody));
        
        const response = await fetch(`${API_BASE_URL}/lab-categories/${categoryId}/manager`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        console.log('响应状态:', response.status);
        
        const result = await response.json();
        console.log('API响应:', result);
        
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('分配实验室管理员失败:', error);
        throw error;
    }
}

/**
 * @description 获取管理员信息
 * @param {number} managerId 
 * @returns {Promise<Object>}
 */
async function getManagerInfo(managerId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/lab-categories/manager/${managerId}/name`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('获取管理员信息失败:', error);
        throw error;
    }
}

/**
 * @description 获取管理员用户列表
 * @returns {Promise<Array>}
 */
async function getAdminUsers() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/users?role=实验室管理员`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        console.log('管理员用户列表API响应:', result);
        
        if (result.code === 200) {
            const users = result.data.records || result.data;
            console.log('管理员用户列表:', users);
            return users;
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('获取管理员用户列表失败:', error);
        throw error;
    }
}

/**
 * @description 验证分类表单
 * @param {Object} formData 
 * @returns {Array}
 */
function validateCategoryForm(formData) {
    const errors = [];
    
    // 验证分类名称
    if (!formData.name || formData.name.trim().length === 0) {
        errors.push('分类名称不能为空');
    } else if (formData.name.length > 50) {
        errors.push('分类名称不能超过50个字符');
    }
    
    // 验证描述
    if (formData.description && formData.description.length > 255) {
        errors.push('分类描述不能超过255个字符');
    }
    
    // 验证管理员ID
    if (formData.adminId && (isNaN(formData.adminId) || formData.adminId <= 0)) {
        errors.push('管理员ID必须是正整数');
    }
    
    if (formData.managerId && (isNaN(formData.managerId) || formData.managerId <= 0)) {
        errors.push('管理员ID必须是正整数');
    }
    
    return errors;
}

/**
 * @description 渲染分类列表
 * @param {Array} categories 
 */
function renderCategoryList(categories) {
    const container = document.getElementById('category-list');
    if (!container) return;
    
    if (!categories || categories.length === 0) {
        container.innerHTML = '<div class="no-data">暂无实验室分类</div>';
        return;
    }
    
    const html = categories.map(category => `
        <div class="category-item" data-id="${category.id}">
            <div class="category-header">
                <h3 class="category-name">${category.name}</h3>
                <div class="category-actions">
                    <button class="btn btn-primary" onclick="editCategory(${category.id})">编辑</button>
                    <button class="btn btn-danger" onclick="deleteCategory(${category.id})">删除</button>
                </div>
            </div>
            <div class="category-content">
                <p class="category-description">${category.description || '暂无描述'}</p>
                <div class="category-admin">
                    <span class="admin-label">管理员:</span>
                    <span class="admin-info">
                        ${category.admin ? 
                            `${category.admin.userName} (${category.admin.userAccount})` : 
                            '未分配'
                        }
                    </span>
                    ${category.admin ? 
                        `<button class="btn btn-small" onclick="reassignManager(${category.id})">重新分配</button>` : 
                        `<button class="btn btn-small" onclick="assignManager(${category.id})">分配管理员</button>`
                    }
                </div>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = html;
}

/**
 * @description 编辑分类
 * @param {number} id 
 */
async function editCategory(id) {
    try {
        const category = await getLabCategory(id);
        showCategoryForm(category);
    } catch (error) {
        alert('获取分类信息失败: ' + error.message);
    }
}

/**
 * @description 删除分类
 * @param {number} id 
 */
async function deleteCategory(id) {
    if (!confirm('确定要删除这个实验室分类吗？删除后不可恢复。')) {
        return;
    }
    
    try {
        await deleteLabCategory(id);
        alert('分类删除成功！');
        loadCategories();
    } catch (error) {
        alert('分类删除失败: ' + error.message);
    }
}

/**
 * @description 分配管理员
 * @param {number} categoryId 
 */
async function assignManager(categoryId) {
    try {
        const admins = await getAdminUsers();
        showManagerAssignForm(categoryId, admins);
    } catch (error) {
        alert('获取管理员列表失败: ' + error.message);
    }
}

/**
 * @description 重新分配管理员
 * @param {number} categoryId 
 */
async function reassignManager(categoryId) {
    try {
        const category = await getLabCategory(categoryId);
        const admins = await getAdminUsers();
        showManagerAssignForm(categoryId, admins, category.admin);
    } catch (error) {
        alert('获取信息失败: ' + error.message);
    }
}

/**
 * @description 显示分类表单
 * @param {Object} category 
 */
function showCategoryForm(category = null) {
    const modal = document.getElementById('category-modal');
    const form = document.getElementById('category-form');
    
    if (category) {
        // 编辑模式
        form.dataset.mode = 'edit';
        form.dataset.id = category.id;
        document.getElementById('form-title').textContent = '编辑实验室分类';
        document.getElementById('name').value = category.name || '';
        document.getElementById('description').value = category.description || '';
        document.getElementById('adminId').value = category.admin ? category.admin.userId : '';
        document.getElementById('managerId').value = category.managerId || '';
    } else {
        // 新建模式
        form.dataset.mode = 'create';
        delete form.dataset.id;
        document.getElementById('form-title').textContent = '新建实验室分类';
        form.reset();
    }
    
    modal.style.display = 'block';
}

/**
 * @description 显示管理员分配表单
 * @param {number} categoryId 
 * @param {Array} admins 
 * @param {Object} currentAdmin 
 */
function showManagerAssignForm(categoryId, admins, currentAdmin = null) {
    const modal = document.getElementById('manager-modal');
    const form = document.getElementById('manager-form');
    
    console.log('显示管理员分配表单:', {
        categoryId: categoryId,
        admins: admins,
        currentAdmin: currentAdmin
    });
    
    form.dataset.categoryId = categoryId;
    
    // 填充管理员选项
    const select = document.getElementById('adminUserId');
    select.innerHTML = '<option value="">请选择管理员</option>';
    
    admins.forEach(admin => {
        const option = document.createElement('option');
        option.value = admin.userId;
        option.textContent = `${admin.userName} (${admin.userAccount})`;
        
        console.log('添加管理员选项:', {
            userId: admin.userId,
            userName: admin.userName,
            userAccount: admin.userAccount
        });
        
        if (currentAdmin && admin.userId === currentAdmin.userId) {
            option.selected = true;
            console.log('设置默认选中:', admin.userName);
        }
        
        select.appendChild(option);
    });
    
    modal.style.display = 'block';
}

/**
 * @description 加载分类列表
 */
async function loadCategories() {
    try {
        const categories = await getLabCategories();
        renderCategoryList(categories);
    } catch (error) {
        console.error('加载分类列表失败:', error);
        const container = document.getElementById('category-list');
        container.innerHTML = '<div class="error">加载分类列表失败</div>';
    }
}

/**
 * @description 处理分类表单提交
 */
async function handleCategorySubmit(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const adminRaw = formData.get('adminId');
    const managerRaw = formData.get('managerId');
    const categoryData = {
        name: formData.get('name'),
        description: formData.get('description'),
        adminId: adminRaw ? parseInt(adminRaw, 10) : null,
        managerId: managerRaw ? parseInt(managerRaw, 10) : null
    };

    const apiPayload = {
        name: categoryData.name,
        description: categoryData.description
    };
    const chosenManagerId = categoryData.managerId || categoryData.adminId;
    if (chosenManagerId != null && !Number.isNaN(chosenManagerId)) {
        apiPayload.managerUserId = chosenManagerId;
    }
    
    // 表单验证
    const errors = validateCategoryForm(categoryData);
    if (errors.length > 0) {
        alert('表单验证失败:\n' + errors.join('\n'));
        return;
    }
    
    try {
        if (form.dataset.mode === 'edit') {
            await updateLabCategory(form.dataset.id, apiPayload);
            alert('分类更新成功！');
        } else {
            await createLabCategory(apiPayload);
            alert('分类创建成功！');
        }
        
        closeModal('category-modal');
        loadCategories();
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

/**
 * @description 处理管理员分配表单提交
 */
async function handleManagerSubmit(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    const categoryId = parseInt(form.dataset.categoryId);
    const managerUserId = parseInt(formData.get('adminUserId'), 10);
    
    if (!categoryId || categoryId <= 0) {
        alert('分类ID无效');
        return;
    }
    
    if (!managerUserId || managerUserId <= 0 || Number.isNaN(managerUserId)) {
        alert('请选择有效的管理员');
        return;
    }
    
    console.log('提交数据:', {
        categoryId: categoryId,
        managerUserId: managerUserId
    });
    
    try {
        await assignLabManager(categoryId, managerUserId);
        alert('管理员分配成功！');
        closeModal('manager-modal');
        loadCategories();
    } catch (error) {
        console.error('管理员分配失败:', error);
        alert('管理员分配失败: ' + error.message);
    }
}

/**
 * @description 关闭模态框
 * @param {string} modalId 
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * @description 初始化页面
 */
document.addEventListener('DOMContentLoaded', () => {
    // 绑定表单提交事件
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
    document.getElementById('manager-form').addEventListener('submit', handleManagerSubmit);
    
    // 绑定关闭按钮事件
    document.querySelectorAll('.close-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal');
            if (modal) {
                modal.style.display = 'none';
            }
        });
    });
    
    // 绑定新建按钮事件
    document.getElementById('new-category-btn').addEventListener('click', () => {
        showCategoryForm();
    });
    
    // 加载分类列表
    loadCategories();
});

/**
 * @description 当选择管理员时自动填充managerId
 */
document.addEventListener('change', (e) => {
    if (e.target.id === 'adminId') {
        const adminId = e.target.value;
        const managerIdInput = document.getElementById('managerId');
        
        if (adminId) {
            managerIdInput.value = adminId;
        } else {
            managerIdInput.value = '';
        }
    }
});

// 导出全局函数供HTML调用
window.editCategory = editCategory;
window.deleteCategory = deleteCategory;
window.assignManager = assignManager;
window.reassignManager = reassignManager;
window.closeModal = closeModal;
