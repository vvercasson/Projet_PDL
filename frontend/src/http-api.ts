import axios, { AxiosResponse, AxiosError } from 'axios';
import { ImageType } from '@/image';

const instance = axios.create({
  baseURL: "/",
  timeout: 15000,
});

const responseBody = (response: AxiosResponse) => response.data;

const requests = {
  get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
  post: (url: string, body: {}) => instance.post(url, body, { headers: { "Content-Type": "multipart/form-data" }, }).then(responseBody),
  put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
  delete: (url: string) => instance.delete(url).then(responseBody)
};

export const api = {
  getImageList: (): Promise<ImageType[]> => requests.get('images', {}),
  getImage: (id: number): Promise<Blob> => requests.get(`images/${id}`, { responseType: "blob" }),
  getImageFilterAllParameters: (id: number, algo: string, first: string, second: string): Promise<Blob> => requests.get(`images/${id}?algorithm=${algo}&first=${first}&second=${second}`,{responseType: "blob"}),
  getImageFilterOneParameters: (id: number, algo: string, first: string): Promise<Blob> => requests.get(`images/${id}?algorithm=${algo}&first=${first}`,{responseType: "blob"}),
  getImageFilterOnlyAlgo: (id: number, algo: string): Promise<Blob> => requests.get(`images/${id}?algorithm=${algo}`,{responseType: "blob"}),  
  createImage: (form: FormData): Promise<ImageType> => requests.post('images', form),
  deleteImage: (id: number): Promise<void> => requests.delete(`images/${id}`),
};