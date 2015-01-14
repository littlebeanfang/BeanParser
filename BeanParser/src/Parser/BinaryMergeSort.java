package Parser;
/** 
 * 采用归并排序求逆序数 
 *  http://blog.csdn.net/dream328/article/details/12093907
 */  
public class BinaryMergeSort {  
  
  public int nixuNum = 0;  
  
  private void merge(int arr[], int start, int mid, int end) {  
    int temp[] = new int[end - start + 1];  
    int s = 0;  
    int ls = start;  
    int rs = mid + 1;  
    while (ls <= mid && rs <= end) {  
      if (arr[ls] <= arr[rs]) {  
        temp[s++] = arr[ls++];  
      } else {  
        temp[s++] = arr[rs++];  
        nixuNum += mid - ls + 1;  
      }  
    }  
    while (ls <= mid) {  
      temp[s++] = arr[ls++];  
    }  
    while (rs <= end) {  
      temp[s++] = arr[rs++];  
    }  
    int j = 0;  
    for (int i = start; i <= end; i++) {  
      arr[i] = temp[j++];  
    }  
  }  
  
  public void mergesort(int arr[], int start, int end) {  
    if (start < end) {  
      mergesort(arr, start, (start + end) / 2);  
      mergesort(arr, (start + end) / 2 + 1, end);  
      merge(arr, start, (start + end) / 2, end);  
    }  
  }  
  /*
  public static void main(String[] args) {  
    int arr[] = { 4, 1, 2, 3, 6, 5 };  
    mergesort(arr, 0, arr.length - 1);  
    for (int i = 0; i < arr.length; i++) {  
      System.out.print(" " + arr[i]);  
    }  
    System.out.println();  
    System.out.println("逆序数为：" + nixuNum);  
  }  
  */
}  