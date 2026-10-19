export interface ImportError {
  rowNumber?: number;
  field?: string;
  message: string;
}

export interface ImportResult {
  totalRows: number;
  successRows: number;
  failedRows: number;
  errors: ImportError[];
}
