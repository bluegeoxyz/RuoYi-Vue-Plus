export type CustomerType = 'PERSON' | 'COMPANY';
export type CustomerLifecycleStatus = 'ACTIVE' | 'INACTIVE' | 'CANCELLED' | 'MERGED';

export interface CustomerQuery extends PageQuery {
  customerNo?: string;
  customerType?: string;
  customerName?: string;
  mobile?: string;
  status?: string;
  lifecycleStatus?: string;
}

export interface CustomerListVO {
  id: number | string;
  customerNo: string;
  customerType: string;
  customerName: string;
  mobile?: string;
  status: string;
  lifecycleStatus: CustomerLifecycleStatus;
  version?: number;
}

export interface CustomerDetailVO extends CustomerListVO {
  gender?: string;
  phone?: string;
  email?: string;
  idType?: string;
  idNo?: string;
}

export interface CustomerSensitiveDetailVO extends CustomerDetailVO { birthDate?: string; }

export interface CustomerEditVO {
  id: number | string;
  customerNo: string;
  customerType: string;
  customerName: string;
  gender?: string;
  birthDate?: string;
  remark?: string;
  version: number;
}

export interface CustomerCreateForm {
  customerType: CustomerType;
  customerName: string;
  gender?: string;
  mobile?: string;
  phone?: string;
  email?: string;
  idType?: string;
  idNo?: string;
  birthDate?: string;
  remark?: string;
}

export interface CustomerUpdateForm {
  id: number | string;
  version: number;
  customerName: string;
  gender?: string;
  mobile?: string;
  phone?: string;
  email?: string;
  birthDate?: string;
  remark?: string;
}

export interface CustomerLifecycleForm {
  id: number | string;
  version: number;
  lifecycleStatus: Exclude<CustomerLifecycleStatus, 'MERGED'>;
}

export interface CustomerCreatedVO {
  id: number | string;
  customerNo: string;
  customerName: string;
  status: string;
  lifecycleStatus: CustomerLifecycleStatus;
  version: number;
}

export interface ImportError { rowNumber?: number; field?: string; message: string; }
export interface ImportResult { totalRows: number; successRows: number; failedRows: number; errors: ImportError[]; }
