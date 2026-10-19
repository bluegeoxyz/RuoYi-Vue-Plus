import type { AxiosPromise } from '@/utils/api-types';
import request from '@/utils/request';
import { saveBlob } from '@/utils/save';
import type { ImportResult } from './types';

const baseUrl = '/property/resource/tree-import';

export const treeImportResource = (projectId: number | string, file: File): AxiosPromise<ImportResult> => {
  const data = new FormData();
  data.append('projectId', String(projectId));
  data.append('file', file);
  return request({ url: baseUrl, method: 'post', data, headers: { repeatSubmit: false } });
};

export const downloadTreeImportTemplate = async () => {
  const blob = await request({ url: `${baseUrl}/template`, method: 'get', responseType: 'blob' });
  saveBlob(new Blob([blob]), `property_resource_tree_import_${Date.now()}.xlsx`);
};
