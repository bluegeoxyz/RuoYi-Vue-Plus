<template>
  <div class="p-2 app-container customer-page">
    <el-card v-show="showSearch" shadow="hover" class="mb-2">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="客户编号" prop="customerNo"><el-input v-model="queryParams.customerNo" clearable @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="客户名称" prop="customerName"><el-input v-model="queryParams.customerName" clearable @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="客户类型" prop="customerType"><el-select v-model="queryParams.customerType" clearable><el-option v-for="item in customerTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="手机号码" prop="mobile"><el-input v-model="queryParams.mobile" clearable @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="生命周期" prop="lifecycleStatus"><el-select v-model="queryParams.lifecycleStatus" clearable><el-option v-for="item in lifecycleOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="启停状态" prop="status"><el-select v-model="queryParams.status" clearable><el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header><el-row :gutter="10" class="mb-2">
        <el-col :span="1.5"><el-button v-hasPermi="['property:customer:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button></el-col>
        <el-col :span="1.5"><el-button v-hasPermi="['property:customer:import']" type="success" plain icon="Upload" @click="openImportDialog">导入</el-button></el-col>
        <el-col :span="1.5"><el-button v-hasPermi="['property:customer:import']" type="info" plain icon="Download" @click="downloadCustomerImportTemplate">下载模板</el-button></el-col>
        <el-col :span="1.5"><el-button v-hasPermi="['property:customer:export']" type="warning" plain icon="Download" @click="handleExport">导出脱敏数据</el-button></el-col>
        <el-col v-if="canSensitiveExport" :span="1.5"><el-button type="danger" plain icon="Download" @click="handleSensitiveExport">导出完整敏感信息</el-button></el-col>
        <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
      </el-row></template>
      <el-alert v-if="accessDenied" title="无企业客户主档访问权限" description="请联系超级管理员检查 Customer Grant；项目管理权限不会自动授予该权限。" type="warning" :closable="false" show-icon class="mb-2" />
      <el-table v-loading="loading" border :data="customerList">
        <el-table-column label="客户编号" prop="customerNo" min-width="150" />
        <el-table-column label="客户类型" width="100"><template #default="scope"><dict-tag :options="customerTypes" :value="scope.row.customerType" /></template></el-table-column>
        <el-table-column label="客户名称" prop="customerName" min-width="160" />
        <el-table-column label="手机号码" min-width="140"><template #default="scope">{{ scope.row.mobile || '-' }}</template></el-table-column>
        <el-table-column label="生命周期" width="110"><template #default="scope"><el-tag :type="lifecycleTag(scope.row.lifecycleStatus)">{{ lifecycleLabel(scope.row.lifecycleStatus) }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="scope"><dict-tag :options="statuses" :value="scope.row.status" /></template></el-table-column>
        <el-table-column label="操作" width="270" fixed="right" align="center"><template #default="scope">
          <el-button v-hasPermi="['property:customer:query']" link type="primary" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-hasPermi="['property:customer:edit']" link type="primary" :disabled="scope.row.lifecycleStatus === 'MERGED'" @click="handleEdit(scope.row)">修改</el-button>
          <el-dropdown v-hasPermi="['property:customer:edit']" class="ml-2" @command="(command: string) => handleLifecycle(scope.row, command)">
            <el-button link type="primary">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown><el-dropdown-menu>
              <el-dropdown-item command="ACTIVE" :disabled="scope.row.lifecycleStatus === 'ACTIVE' || scope.row.lifecycleStatus === 'CANCELLED'">启用</el-dropdown-item>
              <el-dropdown-item command="INACTIVE" :disabled="scope.row.lifecycleStatus !== 'ACTIVE'">停用</el-dropdown-item>
              <el-dropdown-item command="CANCELLED" :disabled="scope.row.lifecycleStatus === 'CANCELLED' || scope.row.lifecycleStatus === 'MERGED'">注销</el-dropdown-item>
            </el-dropdown-menu></template>
          </el-dropdown>
          <el-button v-hasPermi="['property:customer:remove']" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template></el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="formDialog.title" width="760px" append-to-body destroy-on-close @closed="resetForm">
      <el-form ref="customerFormRef" :model="form" :rules="rules" label-width="105px"><el-row :gutter="20">
        <el-col :span="12"><el-form-item label="客户类型" prop="customerType"><el-select v-model="form.customerType" :disabled="formMode === 'edit'"><el-option v-for="item in customerTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" maxlength="128" /></el-form-item></el-col>
        <el-col v-if="form.customerType === 'PERSON'" :span="12"><el-form-item label="性别" prop="gender"><el-select v-model="form.gender" clearable><el-option v-for="item in genders" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        <template v-if="formMode === 'add'">
          <el-col :span="12"><el-form-item label="手机号码" prop="mobile"><el-input v-model="form.mobile" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" maxlength="128" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="证件类型" prop="idType"><el-select v-model="form.idType" clearable><el-option v-for="item in idTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="证件号码" prop="idNo"><el-input v-model="form.idNo" maxlength="128" show-password autocomplete="off" /></el-form-item></el-col>
        </template>
        <el-col :span="12"><el-form-item label="出生日期" prop="birthDate"><el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit /></el-form-item></el-col>
      </el-row></el-form>
      <el-alert v-if="formMode === 'edit'" title="联系方式与证件身份不通过普通编辑修改；强身份纠错需使用后续专门流程。" type="info" :closable="false" class="mb-2" />
      <template #footer><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button><el-button @click="closeFormDialog">取消</el-button></template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="客户批量导入" width="760px" append-to-body destroy-on-close @closed="resetImportDialog">
      <el-alert title="任意一条客户数据校验失败，本次全部不导入。" type="warning" :closable="false" show-icon class="mb-3" />
      <el-upload v-model:file-list="importFileList" :auto-upload="false" :limit="1" accept=".xlsx,.xls" drag :disabled="importLoading">
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon><div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击选择</em></div>
        <template #tip><div class="el-upload__tip">仅支持 xls、xlsx；单次最多导入 2000 条客户数据。文件内容不会写入浏览器持久存储。</div></template>
      </el-upload>
      <div class="mt-3"><el-link type="primary" :underline="false" @click="downloadCustomerImportTemplate">下载客户导入模板</el-link></div>
      <template v-if="importResult">
        <el-alert v-if="importResult.failedRows" :title="`共 ${importResult.totalRows} 条客户数据，发现 ${importResult.errors.length} 个问题，本次写入 0 条`" type="error" :closable="false" show-icon class="mt-4 mb-2" />
        <el-alert v-else :title="`成功导入 ${importResult.successRows} 条客户数据`" type="success" :closable="false" show-icon class="mt-4 mb-2" />
        <el-table v-if="importResult.errors.length" :data="importResult.errors" border max-height="300"><el-table-column label="行号" width="90"><template #default="scope">{{ scope.row.rowNumber ?? '-' }}</template></el-table-column><el-table-column label="字段" prop="field" width="140" /><el-table-column label="错误消息" prop="message" min-width="380" /></el-table>
      </template>
      <template #footer><el-button type="primary" :loading="importLoading" :disabled="!importFileList.length" @click="submitCustomerImport">上传并导入</el-button><el-button @click="importDialogVisible=false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="客户档案详情" width="760px" append-to-body destroy-on-close @closed="clearDetail">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="客户编号">{{ detail.customerNo }}</el-descriptions-item><el-descriptions-item label="客户类型"><dict-tag :options="customerTypes" :value="detail.customerType" /></el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ shownDetail.customerName }}</el-descriptions-item><el-descriptions-item label="性别"><dict-tag :options="genders" :value="shownDetail.gender" /></el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ shownDetail.mobile || '-' }}</el-descriptions-item><el-descriptions-item label="联系电话">{{ shownDetail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ shownDetail.email || '-' }}</el-descriptions-item><el-descriptions-item label="出生日期">{{ sensitiveDetail?.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证件类型"><dict-tag :options="idTypes" :value="shownDetail.idType" /></el-descriptions-item><el-descriptions-item label="证件号码">{{ shownDetail.idNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生命周期"><el-tag :type="lifecycleTag(detail.lifecycleStatus)">{{ lifecycleLabel(detail.lifecycleStatus) }}</el-tag></el-descriptions-item><el-descriptions-item label="状态"><dict-tag :options="statuses" :value="detail.status" /></el-descriptions-item>
      </el-descriptions>
      <div class="mt-3"><el-button v-if="canViewPii && !sensitiveDetail" type="danger" plain :loading="sensitiveLoading" @click="loadSensitiveDetail">查看完整敏感信息</el-button><el-button v-if="sensitiveDetail" type="info" plain @click="clearSensitive">隐藏完整敏感信息</el-button><span v-if="sensitiveDetail" class="ml-2 text-xs text-red-500">敏感信息已审计，关闭窗口后立即清除。</span></div>
    </el-dialog>
  </div>
</template>

<script setup name="Customer" lang="ts">
import { addCustomer, changeCustomerLifecycle, delCustomer, downloadCustomerImportTemplate, exportCustomer, exportCustomerSensitive, getCustomer, getCustomerEdit, getCustomerSensitive, importCustomer, listCustomer, updateCustomer } from '@/api/property/customer';
import type { CustomerCreateForm, CustomerDetailVO, CustomerLifecycleStatus, CustomerListVO, CustomerQuery, CustomerSensitiveDetailVO, CustomerUpdateForm, ImportResult } from '@/api/property/customer/types';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import auth from '@/plugins/auth';
import modal from '@/plugins/modal';
import { useDict } from '@/utils/dict';
import type { FormRules, UploadUserFile } from 'element-plus';

type CustomerFormModel = CustomerCreateForm & { id?: number | string; version?: number };
type LifecycleCommand = Exclude<CustomerLifecycleStatus, 'MERGED'>;
const dicts = useDict('pm_customer_type', 'zbm_id_type', 'sys_user_gender', 'sys_normal_disable');
const customerTypes = computed(() => dicts.pm_customer_type || []); const idTypes = computed(() => dicts.zbm_id_type || []);
const genders = computed(() => dicts.sys_user_gender || []); const statuses = computed(() => dicts.sys_normal_disable || []);
const lifecycleOptions = [{ label: '正常', value: 'ACTIVE' }, { label: '停用', value: 'INACTIVE' }, { label: '注销', value: 'CANCELLED' }, { label: '已合并', value: 'MERGED' }];
const lifecycleLabel = (value: CustomerLifecycleStatus) => lifecycleOptions.find(item => item.value === value)?.label || value;
const lifecycleTag = (value: CustomerLifecycleStatus): 'success' | 'warning' | 'danger' | 'info' => value === 'ACTIVE' ? 'success' : value === 'INACTIVE' ? 'warning' : value === 'CANCELLED' ? 'danger' : 'info';
const emptyForm = (): CustomerFormModel => ({ customerType: 'PERSON', customerName: '' });
const customerList = ref<CustomerListVO[]>([]); const total = ref(0); const loading = ref(false); const submitLoading = ref(false); const accessDenied = ref(false); const pageActive = ref(true);
const { showSearch } = useSearchToggle(); const queryFormRef = ref<ElFormInstance>(); const customerFormRef = ref<ElFormInstance>();
const queryParams = reactive<CustomerQuery>({ pageNum: 1, pageSize: 10 }); const form = reactive<CustomerFormModel>(emptyForm());
const formMode = ref<'add' | 'edit'>('add'); const formDialog = reactive({ visible: false, title: '' });
const detailVisible = ref(false); const detail = ref<CustomerDetailVO>(); const sensitiveDetail = ref<CustomerSensitiveDetailVO>(); const sensitiveLoading = ref(false);
const importDialogVisible=ref(false);const importLoading=ref(false);const importFileList=ref<UploadUserFile[]>([]);const importResult=ref<ImportResult>();let importVersion=0;
const canViewPii = computed(() => auth.hasPermi('property:customer:pii:view'));
const canSensitiveExport = computed(() => auth.hasPermi('property:customer:export') && auth.hasPermi('property:customer:pii:export'));
let listVersion = 0; let detailVersion = 0; let piiVersion = 0; let activationCount = 0;
const rules: FormRules = { customerType: [{ required: true, message: '客户类型不能为空', trigger: 'change' }], customerName: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }, { max: 128, message: '客户名称不能超过128个字符', trigger: 'blur' }], email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }] };
const traceId = () => `customer-pii-${Date.now()}-${crypto.randomUUID()}`;
const isAccessDenied = (error: unknown) => error instanceof Error && (error.message.includes('无企业客户主档授权') || error.message.includes('无客户主档功能权限'));
const isConflict = (error: unknown) => error instanceof Error && error.message.includes('已被其他请求修改');
const invalidate = () => { listVersion++; detailVersion++; piiVersion++; loading.value = false; sensitiveLoading.value = false; };
const clearSensitive = () => { piiVersion++; sensitiveDetail.value = undefined; sensitiveLoading.value = false; };
const clearDetail = () => { detailVersion++; clearSensitive(); detail.value = undefined; detailVisible.value = false; };
const resetForm = () => { customerFormRef.value?.resetFields(); Object.assign(form, emptyForm()); for (const key of Object.keys(form)) if (!(key in emptyForm())) delete form[key as keyof CustomerFormModel]; };
const closeFormDialog = () => { formDialog.visible = false; resetForm(); };
const revoke = () => { customerList.value = []; total.value = 0; accessDenied.value = true; clearDetail(); closeFormDialog(); };
const shownDetail = computed<CustomerDetailVO>(() => sensitiveDetail.value || detail.value || { id: '', customerNo: '', customerType: '', customerName: '', status: '', lifecycleStatus: 'INACTIVE', version: 0 });

const getList = async () => { const version = ++listVersion; loading.value = true; try { const res = await listCustomer({ ...queryParams }); if (!pageActive.value || version !== listVersion) return; customerList.value = res.data?.rows || []; total.value = res.data?.total || 0; accessDenied.value = false; } catch (error) { if (version === listVersion && isAccessDenied(error)) revoke(); } finally { if (version === listVersion) loading.value = false; } };
const handleQuery = () => { queryParams.pageNum = 1; getList(); }; const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleAdd = () => { resetForm(); formMode.value = 'add'; formDialog.title = '新增客户档案'; formDialog.visible = true; };
const handleEdit = async (row: Partial<CustomerListVO>) => { if (!row.id) return; const version = ++detailVersion; clearSensitive(); try { const res = await getCustomerEdit(row.id); if (!pageActive.value || version !== detailVersion) return; resetForm(); Object.assign(form, res.data); formMode.value = 'edit'; formDialog.title = '修改客户普通资料'; formDialog.visible = true; } catch (error) { if (isAccessDenied(error)) revoke(); } };
const handleDetail = async (row: Partial<CustomerListVO>) => { if (!row.id) return; const version = ++detailVersion; clearSensitive(); detail.value = undefined; detailVisible.value = true; try { const res = await getCustomer(row.id); if (!pageActive.value || version !== detailVersion || !detailVisible.value) return; detail.value = res.data; } catch (error) { if (version === detailVersion) clearDetail(); if (isAccessDenied(error)) revoke(); } };
const loadSensitiveDetail = async () => { const id = detail.value?.id; if (!id) return; const version = ++piiVersion; sensitiveDetail.value = undefined; sensitiveLoading.value = true; try { const res = await getCustomerSensitive(id, traceId()); if (!pageActive.value || version !== piiVersion || !detailVisible.value || String(detail.value?.id) !== String(id)) return; sensitiveDetail.value = res.data; } catch (error) { if (version === piiVersion) clearSensitive(); if (isAccessDenied(error)) revoke(); } finally { if (version === piiVersion) sensitiveLoading.value = false; } };
const submitForm = () => customerFormRef.value?.validate(async valid => { if (!valid) return; submitLoading.value = true; try { if (formMode.value === 'add') { const payload: CustomerCreateForm = { customerType: form.customerType, customerName: form.customerName, gender: form.gender, mobile: form.mobile, phone: form.phone, email: form.email, idType: form.idType, idNo: form.idNo, birthDate: form.birthDate, remark: form.remark }; await addCustomer(payload); } else { const payload: CustomerUpdateForm = { id: form.id!, version: form.version!, customerName: form.customerName, gender: form.gender, birthDate: form.birthDate, remark: form.remark }; await updateCustomer(payload); } modal.msgSuccess('操作成功'); closeFormDialog(); await getList(); } catch (error) { if (isAccessDenied(error)) revoke(); else if (formMode.value === 'edit' && isConflict(error) && form.id) { modal.msgWarning('数据已被其他用户修改，已重新加载最新资料，请确认后再次提交'); const latest = await getCustomerEdit(form.id); Object.assign(form, latest.data); } } finally { submitLoading.value = false; if (!formDialog.visible) form.idNo = undefined; } });
const handleLifecycle = async (row: Partial<CustomerListVO>, command: string) => { if (!row.id || row.version === undefined) return; const target = command as LifecycleCommand; const label = lifecycleLabel(target); await modal.confirm(`确认将客户“${row.customerNo || ''}”变更为${label}吗？此操作不会删除历史房屋关系。`); try { await changeCustomerLifecycle({ id: row.id, version: row.version, lifecycleStatus: target }); modal.msgSuccess(`${label}成功`); await getList(); } catch (error) { if (isAccessDenied(error)) revoke(); } };
const handleDelete = async (row: Partial<CustomerListVO>) => { if (!row.id || row.version === undefined) return; await modal.confirm('确认删除该客户档案吗？存在任何历史房屋关系的客户无法删除，只能停用或注销。'); try { await delCustomer(row.id, row.version); modal.msgSuccess('删除成功'); await getList(); } catch (error) { if (isAccessDenied(error)) revoke(); } };
const handleExport = () => exportCustomer({ ...queryParams });
const handleSensitiveExport = async () => { await modal.confirm('导出文件将包含完整敏感客户资料，本次操作会被安全审计。确认继续吗？'); await exportCustomerSensitive({ ...queryParams }); };
const resetImportDialog=()=>{importVersion++;importFileList.value=[];importResult.value=undefined;importLoading.value=false;};
const openImportDialog=()=>{resetImportDialog();importDialogVisible.value=true;};
const submitCustomerImport=async()=>{const file=importFileList.value[0]?.raw;if(!file)return modal.msgWarning('请选择 Excel 文件');const version=++importVersion;importLoading.value=true;importResult.value=undefined;try{const res=await importCustomer(file);if(version!==importVersion||!importDialogVisible.value)return;importResult.value=res.data;if(!res.data.failedRows){modal.msgSuccess('导入成功');importFileList.value=[];await getList();}}finally{if(version===importVersion)importLoading.value=false;}};
onMounted(getList); onActivated(() => { pageActive.value = true; if (activationCount++ > 0) getList(); });
onDeactivated(() => { pageActive.value = false; invalidate(); clearSensitive(); resetImportDialog();importDialogVisible.value=false; }); onBeforeUnmount(() => { pageActive.value = false; invalidate(); clearDetail(); resetForm();resetImportDialog(); });
</script>

<style scoped lang="scss">.customer-page :deep(.el-select), .customer-page :deep(.el-date-editor) { width: 220px; }</style>
