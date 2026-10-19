<template>
  <div class="p-2 app-container room-page">
    <el-alert v-if="!currentProjectId" title="请先在顶部选择当前物业项目" type="info" :closable="false" show-icon class="mb-2" />
    <el-alert v-else-if="resourcePermissionInvalid" title="当前项目的房屋访问权限已失效" description="请重新选择具有 RESOURCE 业务访问权限的项目。" type="warning" :closable="false" show-icon class="mb-2" />
    <template v-else>
      <el-card v-show="showSearch" shadow="hover" class="mb-2">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true">
          <el-form-item label="所属楼栋" prop="buildingId">
            <el-select v-model="queryParams.buildingId" clearable filterable placeholder="请选择楼栋" @change="handleQueryBuildingChange">
              <el-option v-for="item in buildingOptionsList" :key="item.id" :label="buildingLabel(item)" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属单元" prop="unitId">
            <el-select v-model="queryParams.unitId" clearable filterable placeholder="请选择单元" :disabled="!queryParams.buildingId" @change="handleQuery">
              <el-option v-for="item in unitOptionsList" :key="item.id" :label="unitLabel(item)" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="房屋编码" prop="roomCode"><el-input v-model="queryParams.roomCode" clearable placeholder="请输入房屋编码" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="房号" prop="roomNo"><el-input v-model="queryParams.roomNo" clearable placeholder="请输入房号" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" clearable placeholder="请选择状态"><el-option label="正常" value="0" /><el-option label="停用" value="1" /></el-select></el-form-item>
          <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
        </el-form>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10" class="mb-2">
            <el-col :span="1.5"><el-button v-hasPermi="['property:room:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button></el-col>
            <el-col :span="1.5"><el-button v-hasPermi="['property:room:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button></el-col>
            <el-col :span="1.5"><el-button v-hasPermi="['property:room:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button></el-col>
            <el-col :span="1.5"><el-button v-hasPermi="['property:room:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button></el-col>
            <el-col :span="1.5"><el-button v-hasPermi="['property:resource:treeImport']" type="info" plain icon="Download" :disabled="!currentProjectId" @click="downloadImportTemplate">下载资源模板</el-button></el-col>
            <el-col :span="1.5"><el-button v-hasPermi="['property:resource:treeImport']" type="primary" plain icon="Upload" :disabled="!currentProjectId" @click="openImportDialog">导入房屋资源</el-button></el-col>
            <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
          </el-row>
        </template>
        <el-table v-loading="loading" border :data="roomList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="楼栋" min-width="150"><template #default="scope">{{ buildingName(scope.row.buildingId) }}</template></el-table-column>
          <el-table-column label="单元" min-width="130"><template #default="scope">{{ unitName(scope.row.unitId) }}</template></el-table-column>
          <el-table-column label="房屋编码" prop="roomCode" min-width="120"><template #default="scope"><el-button v-hasPermi="['property:room:query']" link type="primary" @click="handleDetail(scope.row)">{{ scope.row.roomCode }}</el-button></template></el-table-column>
          <el-table-column label="房号" prop="roomNo" min-width="100" />
          <el-table-column label="楼层" prop="floorNo" width="80" align="center" />
          <el-table-column label="建筑面积" prop="buildingArea" width="110" align="right" />
          <el-table-column label="入住状态" prop="occupancyStatus" width="110"><template #default="scope">{{ scope.row.occupancyStatus || '-' }}</template></el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><el-switch v-model="scope.row.status" v-hasPermi="['property:room:edit']" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)" /></template></el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center"><template #default="scope">
            <el-button v-hasPermi="['property:room:query']" link type="primary" icon="View" @click="handleDetail(scope.row)" />
            <el-button v-hasPermi="['property:room:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" />
            <el-button v-hasPermi="['property:room:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)" />
          </template></el-table-column>
        </el-table>
        <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </el-card>
    </template>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="760px" append-to-body>
      <el-form ref="roomFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="所属楼栋" prop="buildingId"><el-select v-model="form.buildingId" filterable :disabled="Boolean(form.id)" @change="handleFormBuildingChange"><el-option v-for="item in buildingOptionsList" :key="item.id" :label="buildingLabel(item)" :value="item.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属单元" prop="unitId"><el-select v-model="form.unitId" filterable :disabled="Boolean(form.id) || !form.buildingId"><el-option v-for="item in formUnitOptions" :key="item.id" :label="unitLabel(item)" :value="item.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房屋编码" prop="roomCode"><el-input v-model="form.roomCode" maxlength="64" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房号" prop="roomNo"><el-input v-model="form.roomNo" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="楼层" prop="floorNo"><el-input-number v-model="form.floorNo" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房屋类型" prop="roomType"><el-input v-model="form.roomType" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产权属性" prop="propertyType"><el-input v-model="form.propertyType" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="建筑面积" prop="buildingArea"><el-input-number v-model="form.buildingArea" :min="0" :precision="2" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="使用面积" prop="usableArea"><el-input-number v-model="form.usableArea" :min="0" :precision="2" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="交付状态" prop="deliveryStatus"><el-input v-model="form.deliveryStatus" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入住状态" prop="occupancyStatus"><el-input v-model="form.occupancyStatus" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="显示顺序" prop="sort"><el-input-number v-model="form.sort" :min="0" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button><el-button @click="cancel">取消</el-button></template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="导入房屋资源" width="760px" append-to-body @closed="resetImportDialog">
      <el-descriptions :column="1" border class="mb-3">
        <el-descriptions-item label="当前项目">{{ importProjectName }}</el-descriptions-item>
      </el-descriptions>
      <el-alert title="任意一行数据校验失败，本次全部不导入，数据库写入 0 条。" type="warning" :closable="false" show-icon class="mb-3" />
      <el-upload v-model:file-list="importFileList" :auto-upload="false" :limit="1" accept=".xlsx,.xls" drag :disabled="importLoading">
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击选择</em></div>
        <template #tip><div class="el-upload__tip">仅支持 xls、xlsx；单次最多导入 2000 条房屋数据。</div></template>
      </el-upload>
      <div class="mt-3"><el-link type="primary" :underline="false" @click="downloadImportTemplate">下载房屋资源导入模板</el-link></div>
      <template v-if="importResult">
        <el-alert v-if="importResult.failedRows > 0" title="本次导入失败，数据库写入 0 条。" type="error" :closable="false" show-icon class="mt-4 mb-2" />
        <el-alert v-else title="房屋资源导入成功" type="success" :closable="false" show-icon class="mt-4 mb-2" />
        <el-descriptions :column="3" border class="mb-2">
          <el-descriptions-item label="总行数">{{ importResult.totalRows }}</el-descriptions-item>
          <el-descriptions-item label="成功行数">{{ importResult.successRows }}</el-descriptions-item>
          <el-descriptions-item label="失败行数">{{ importResult.failedRows }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="importResult.errors.length" :data="importResult.errors" border max-height="300">
          <el-table-column label="行号" prop="rowNumber" width="90"><template #default="scope">{{ scope.row.rowNumber ?? '-' }}</template></el-table-column>
          <el-table-column label="字段" prop="field" width="150"><template #default="scope">{{ scope.row.field || '-' }}</template></el-table-column>
          <el-table-column label="错误消息" prop="message" min-width="360" show-overflow-tooltip />
        </el-table>
      </template>
      <template #footer><el-button type="primary" :loading="importLoading" :disabled="!importFileList.length" @click="submitTreeImport">上传并导入</el-button><el-button @click="importDialogVisible=false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="房屋详情" width="700px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="所属楼栋">{{ buildingName(detail.buildingId) }}</el-descriptions-item><el-descriptions-item label="所属单元">{{ unitName(detail.unitId) }}</el-descriptions-item>
        <el-descriptions-item label="房屋编码">{{ detail.roomCode }}</el-descriptions-item><el-descriptions-item label="房号">{{ detail.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ detail.floorNo ?? '-' }}</el-descriptions-item><el-descriptions-item label="房屋类型">{{ detail.roomType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产权属性">{{ detail.propertyType || '-' }}</el-descriptions-item><el-descriptions-item label="建筑面积">{{ detail.buildingArea ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="使用面积">{{ detail.usableArea ?? '-' }}</el-descriptions-item><el-descriptions-item label="交付状态">{{ detail.deliveryStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住状态">{{ detail.occupancyStatus || '-' }}</el-descriptions-item><el-descriptions-item label="状态">{{ detail.status === '0' ? '正常' : '停用' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="Room" lang="ts">
import type { UploadUserFile } from 'element-plus';
import { buildingOptions, resourceProjectOptions } from '@/api/property/resource/building';
import type { BuildingOptionVO } from '@/api/property/resource/building/types';
import { buildingUnitOptions } from '@/api/property/resource/unit';
import type { BuildingUnitOptionVO } from '@/api/property/resource/unit/types';
import { addRoom, changeRoomStatus, delRoom, exportRoom, getRoom, listRoom, updateRoom } from '@/api/property/resource/room';
import type { RoomForm, RoomQuery, RoomVO } from '@/api/property/resource/room/types';
import { downloadTreeImportTemplate, treeImportResource } from '@/api/property/resource/treeImport';
import type { ImportResult } from '@/api/property/resource/treeImport/types';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';
import { useCurrentProjectStore } from '@/store/modules/currentProject';

const currentProjectStore = useCurrentProjectStore();
const currentProjectId = computed(() => currentProjectStore.currentProjectId);
const roomList = ref<RoomVO[]>([]); const buildingOptionsList = ref<BuildingOptionVO[]>([]); const unitOptionsList = ref<BuildingUnitOptionVO[]>([]); const formUnitOptions = ref<BuildingUnitOptionVO[]>([]);
const detail = ref<RoomVO>(); const detailVisible = ref(false); const ids = ref<Array<number | string>>([]); const single = ref(true); const multiple = ref(true); const total = ref(0);
const importDialogVisible = ref(false); const importLoading = ref(false); const importFileList = ref<UploadUserFile[]>([]); const importResult = ref<ImportResult>(); const importProjectId = ref<number|string>(); const importProjectName = ref(''); let importVersion = 0;
const loading = ref(false); const submitLoading = ref(false); const dialogProjectId = ref<number | string>(); const resourcePermissionInvalid = ref(false); const resourceValidatedProjectId = ref<number | string>(); const pageActive = ref(true);
let requestVersion = 0; let candidateVersion = 0; let activationCount = 0;
const { showSearch } = useSearchToggle(); const queryFormRef = ref<ElFormInstance>(); const roomFormRef = ref<ElFormInstance>();
const initFormData = (): RoomForm => ({ id: undefined, projectId: undefined, buildingId: undefined, unitId: undefined, roomCode: '', roomNo: '', floorNo: undefined, roomType: undefined, propertyType: undefined, buildingArea: undefined, usableArea: undefined, deliveryStatus: 'UNDELIVERED', occupancyStatus: 'VACANT', sort: 0, status: '0', remark: '' });
const data = reactive<PageData<RoomForm, RoomQuery>>({ form: initFormData(), queryParams: { pageNum: 1, pageSize: 10, projectId: undefined, buildingId: undefined, unitId: undefined, roomCode: undefined, roomNo: undefined, status: undefined }, rules: {
  buildingId: [{ required: true, message: '所属楼栋不能为空', trigger: 'change' }], unitId: [{ required: true, message: '所属单元不能为空', trigger: 'change' }],
  roomCode: [{ required: true, message: '房屋编码不能为空', trigger: 'blur' }, { max: 64, message: '房屋编码长度不能超过64个字符', trigger: 'blur' }],
  roomNo: [{ required: true, message: '房号不能为空', trigger: 'blur' }, { max: 32, message: '房号长度不能超过32个字符', trigger: 'blur' }]
} });
const { queryParams, form, rules } = toRefs(data); const { dialog, resetForm, openDialog, showDialog, closeDialog } = useFormDialog({ form, formRef: roomFormRef, initialFormData: initFormData() });
const buildingLabel = (item: BuildingOptionVO) => `${item.buildingCode} - ${item.buildingName}`; const unitLabel = (item: BuildingUnitOptionVO) => `${item.unitCode} - ${item.unitName}`;
const buildingName = (id: number | string) => { const item=buildingOptionsList.value.find(x=>String(x.id)===String(id)); return item?buildingLabel(item):String(id); };
const unitName = (id: number | string) => { const item=[...unitOptionsList.value,...formUnitOptions.value].find(x=>String(x.id)===String(id)); return item?unitLabel(item):String(id); };
const clearList = () => { roomList.value=[];total.value=0;ids.value=[];single.value=true;multiple.value=true; };
const cancel = () => { closeDialog();resetForm();formUnitOptions.value=[];dialogProjectId.value=undefined; };
const resetProjectState = () => { clearList();buildingOptionsList.value=[];unitOptionsList.value=[];queryParams.value.buildingId=undefined;queryParams.value.unitId=undefined;cancel();detail.value=undefined;detailVisible.value=false; };
const invalidateRequests = () => { requestVersion++;candidateVersion++;loading.value=false; };
const isCurrentRequest = (v:number,p:number|string) => pageActive.value&&v===requestVersion&&String(currentProjectId.value)===String(p);
const isBusinessPermissionError = (e:unknown) => e instanceof Error&&e.message.includes('无项目业务访问权限');
const validateResourceProject = async (p:number|string,v:number) => { const res=await resourceProjectOptions();if(!isCurrentRequest(v,p))return false;const allowed=(res.data||[]).some(x=>String(x.id)===String(p));resourceValidatedProjectId.value=allowed?p:undefined;resourcePermissionInvalid.value=!allowed;if(!allowed)resetProjectState();return allowed; };
const loadBuildings = async (p:number|string,v:number) => { const res=await buildingOptions({projectId:p});if(!isCurrentRequest(v,p))return false;buildingOptionsList.value=res.data||[];return true; };
const loadQueryUnits = async (p:number|string,b:number|string|undefined,v:number) => { if(!b){unitOptionsList.value=[];return true;}const res=await buildingUnitOptions({projectId:p,buildingId:b});if(!isCurrentRequest(v,p))return false;unitOptionsList.value=res.data||[];return true; };
const handlePermissionRevoked = async (p:number|string,v:number) => { resetProjectState();try{const allowed=await validateResourceProject(p,v);if(isCurrentRequest(v,p)&&!allowed)modal.msgWarning('当前项目的房屋访问权限已失效，请重新选择项目');}catch{/* 网络异常不视为撤权 */} };
const handleOperationError = async (e:unknown) => { const p=currentProjectId.value;if(p&&isBusinessPermissionError(e))await handlePermissionRevoked(p,++requestVersion); };
const getList = async () => { const p=currentProjectId.value;const v=++requestVersion;if(!p){resetProjectState();resourcePermissionInvalid.value=false;resourceValidatedProjectId.value=undefined;return;}loading.value=true;try{if(!(await validateResourceProject(p,v))||!(await loadBuildings(p,v))||!(await loadQueryUnits(p,queryParams.value.buildingId,v)))return;const res=await listRoom({...queryParams.value,projectId:p});if(!isCurrentRequest(v,p))return;roomList.value=res.data?.rows||[];total.value=res.data?.total||0;}catch(e){if(isCurrentRequest(v,p)&&isBusinessPermissionError(e))await handlePermissionRevoked(p,v);}finally{if(v===requestVersion)loading.value=false;} };
const handleQuery = () => { queryParams.value.pageNum=1;getList(); }; const handleQueryBuildingChange = () => { queryParams.value.unitId=undefined;unitOptionsList.value=[];handleQuery(); }; const resetQuery = () => { queryFormRef.value?.resetFields();unitOptionsList.value=[];handleQuery(); };
const handleSelectionChange = (rows:RoomVO[]) => { ids.value=rows.map(x=>x.id);single.value=rows.length!==1;multiple.value=rows.length===0; };
const handleAdd = () => { if(!currentProjectId.value||resourcePermissionInvalid.value)return modal.msgWarning('请先选择具有资源权限的当前项目');if(!buildingOptionsList.value.length)return modal.msgWarning('当前项目没有可用楼栋');resetForm();form.value.projectId=currentProjectId.value;dialogProjectId.value=currentProjectId.value;openDialog('新增房屋'); };
const handleFormBuildingChange = async () => { form.value.unitId=undefined;formUnitOptions.value=[];const p=currentProjectId.value,b=form.value.buildingId,v=++candidateVersion;if(!p||!b)return;try{const res=await buildingUnitOptions({projectId:p,buildingId:b});if(v===candidateVersion&&String(p)===String(currentProjectId.value)&&String(b)===String(form.value.buildingId))formUnitOptions.value=res.data||[];}catch(e){await handleOperationError(e);} };
const loadDetail = async (id:number|string,mode:'edit'|'detail') => { const p=currentProjectId.value;if(!p||resourcePermissionInvalid.value)return;const v=++requestVersion;try{if(!(await validateResourceProject(p,v))||!(await loadBuildings(p,v)))return;const res=await getRoom(id);if(!isCurrentRequest(v,p))return;const units=await buildingUnitOptions({projectId:p,buildingId:res.data.buildingId});if(!isCurrentRequest(v,p))return;formUnitOptions.value=units.data||[];if(mode==='edit'){resetForm();Object.assign(form.value,res.data);dialogProjectId.value=p;showDialog('修改房屋');}else{detail.value=res.data;detailVisible.value=true;}}catch(e){if(isCurrentRequest(v,p)&&isBusinessPermissionError(e))await handlePermissionRevoked(p,v);} };
const handleUpdate = (row?:Partial<RoomVO>) => { const id=row?.id??ids.value[0];if(id)loadDetail(id,'edit'); }; const handleDetail = (row:Partial<RoomVO>) => { if(row.id)loadDetail(row.id,'detail'); };
const submitForm = () => roomFormRef.value?.validate(async valid => { if(!valid)return;if(!currentProjectId.value||String(dialogProjectId.value)!==String(currentProjectId.value)){modal.msgWarning('当前项目已切换，请重新操作');cancel();return;}submitLoading.value=true;try{if(form.value.id)await updateRoom(form.value);else await addRoom({...form.value,projectId:currentProjectId.value});modal.msgSuccess('操作成功');cancel();await getList();}catch(e){await handleOperationError(e);}finally{submitLoading.value=false;} });
const handleStatusChange = async (row:Partial<RoomVO>) => { if(!row.id)return;const target=row.status||'1';try{await modal.confirm(`确认${target==='0'?'启用':'停用'}房屋“${row.roomNo}”吗？`);await changeRoomStatus({id:row.id,status:target});modal.msgSuccess('操作成功');}catch(e){row.status=target==='0'?'1':'0';await handleOperationError(e);} };
const handleDelete = async (row?:Partial<RoomVO>) => { const target=row?.id??ids.value;if(Array.isArray(target)&&!target.length)return;await modal.confirm('确认删除选中的房屋吗？存在有效客户关系的房屋无法删除。');try{await delRoom(target);modal.msgSuccess('删除成功');await getList();}catch(e){await handleOperationError(e);} };
const handleExport = () => { if(currentProjectId.value&&!resourcePermissionInvalid.value)exportRoom({...queryParams.value,projectId:currentProjectId.value}); };
const resetImportDialog = () => { importFileList.value=[];importResult.value=undefined;importProjectId.value=undefined;importProjectName.value='';importLoading.value=false; };
const openImportDialog = () => { if(!currentProjectId.value)return modal.msgWarning('请先选择当前物业项目');importVersion++;resetImportDialog();importProjectId.value=currentProjectId.value;importProjectName.value=currentProjectStore.currentProjectName;importDialogVisible.value=true; };
const downloadImportTemplate = async () => { if(!currentProjectId.value)return modal.msgWarning('请先选择当前物业项目');await downloadTreeImportTemplate(); };
const submitTreeImport = async () => { const file=importFileList.value[0]?.raw,p=importProjectId.value,v=++importVersion;if(!file||!p)return modal.msgWarning('请选择 Excel 文件');if(String(p)!==String(currentProjectId.value)){importDialogVisible.value=false;return modal.msgWarning('当前项目已切换，请重新选择文件');}importLoading.value=true;importResult.value=undefined;try{const res=await treeImportResource(p,file);if(v!==importVersion||String(p)!==String(currentProjectId.value)||!importDialogVisible.value)return;importResult.value=res.data;if(res.data.failedRows===0){modal.msgSuccess('导入成功');await getList();}}catch(e){if(v===importVersion&&String(p)===String(currentProjectId.value))await handleOperationError(e);}finally{if(v===importVersion)importLoading.value=false;} };
const stopProjectWatch = watch(currentProjectId,()=>{importVersion++;importDialogVisible.value=false;resetImportDialog();invalidateRequests();resetProjectState();resourcePermissionInvalid.value=false;resourceValidatedProjectId.value=undefined;queryParams.value.pageNum=1;if(pageActive.value)getList();});
onMounted(getList);onActivated(()=>{pageActive.value=true;if(activationCount++===0)return;if(String(resourceValidatedProjectId.value||'')!==String(currentProjectId.value||''))resetProjectState();getList();});onDeactivated(()=>{pageActive.value=false;invalidateRequests();});onBeforeUnmount(()=>{pageActive.value=false;invalidateRequests();stopProjectWatch();});
</script>

<style lang="scss" scoped>
.room-page :deep(.el-select) { width: 220px; }
.room-page :deep(.el-input-number) { width: 100%; }
</style>
