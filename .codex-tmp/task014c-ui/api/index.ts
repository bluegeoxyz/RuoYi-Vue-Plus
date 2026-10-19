import type { PageResult } from '@/api/types';
import type { AxiosPromise } from '@/utils/api-types';
import request, { download } from '@/utils/request';
import { saveBlob } from '@/utils/save';
import type { ImportResult } from './types';
import type { CustomerCreateForm, CustomerCreatedVO, CustomerDetailVO, CustomerEditVO, CustomerLifecycleForm, CustomerListVO, CustomerQuery, CustomerSensitiveDetailVO, CustomerUpdateForm } from './types';

export const listCustomer = (query: CustomerQuery): AxiosPromise<PageResult<CustomerListVO>> => request({ url: '/property/customer/list', method: 'get', params: query });
export const getCustomer = (id: number | string): AxiosPromise<CustomerDetailVO> => request({ url: `/property/customer/${id}`, method: 'get' });
export const getCustomerEdit = (id: number | string): AxiosPromise<CustomerEditVO> => request({ url: `/property/customer/${id}/edit`, method: 'get' });
export const getCustomerSensitive = (id: number | string, traceId: string): AxiosPromise<CustomerSensitiveDetailVO> => request({ url: `/property/customer/${id}/sensitive`, method: 'get', headers: { 'X-Trace-Id': traceId } });
export const addCustomer = (data: CustomerCreateForm): AxiosPromise<CustomerCreatedVO> => request({ url: '/property/customer', method: 'post', data });
export const updateCustomer = (data: CustomerUpdateForm) => request({ url: '/property/customer', method: 'put', data });
export const changeCustomerLifecycle = (data: CustomerLifecycleForm) => request({ url: '/property/customer/changeStatus', method: 'put', data });
export const delCustomer = (id: number | string, version: number) => request({ url: `/property/customer/${id}`, method: 'delete', params: { version } });
export const exportCustomer = (query: CustomerQuery) => download('/property/customer/export', query, `customer_masked_${Date.now()}.xlsx`);
export const exportCustomerSensitive = (query: CustomerQuery) => download('/property/customer/export-sensitive', query, `customer_sensitive_${Date.now()}.xlsx`);
export const importCustomer = (file: File): AxiosPromise<ImportResult> => { const data=new FormData();data.append('file',file);return request({url:'/property/customer/import',method:'post',data,headers:{repeatSubmit:false}}); };
export const downloadCustomerImportTemplate = async () => { const blob=await request({url:'/property/customer/import/template',method:'get',responseType:'blob'});saveBlob(new Blob([blob]),`customer_import_${Date.now()}.xlsx`); };
