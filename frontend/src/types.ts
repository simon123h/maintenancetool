export interface UserAccount {
  id: string;
  mailAdresse: string;
  vollerName: string;
  loginName: string;
  rollen: string[];
}

export interface StoredFile {
  id: string;
  originalDateiname: string;
  dateipfad: string;
  contentType: string;
  dateigroesse: number;
  uploadDatum: string;
}

export interface Branding {
  companyName: string;
  logoPath: string;
  themeColor: string;
  themeColorHover: string;
}
