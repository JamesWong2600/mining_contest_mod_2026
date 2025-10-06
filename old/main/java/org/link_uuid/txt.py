import os
import sys

def change_file_extensions(root_folder):
    """
    將指定資料夾及其所有子資料夾中的 .java 副檔名更改為 .txt
    """
    changed_files = []
    
    # 遍歷資料夾及其所有子資料夾
    for foldername, subfolders, filenames in os.walk(root_folder):
        for filename in filenames:
            # 檢查檔案是否以 .java 結尾
            if filename.endswith('.java'):
                # 取得檔案的完整路徑
                old_file_path = os.path.join(foldername, filename)
                
                # 建立新的檔案名稱（將 .java 替換為 .txt）
                new_filename = filename[:-5] + '.txt'  # 移除 .java (5個字元)
                new_file_path = os.path.join(foldername, new_filename)
                
                try:
                    # 重新命名檔案
                    os.rename(old_file_path, new_file_path)
                    changed_files.append((old_file_path, new_file_path))
                    print(f"已更改: {old_file_path} -> {new_file_path}")
                except Exception as e:
                    print(f"錯誤: 無法重新命名 {old_file_path} - {e}")
    
    return changed_files

def main():
    # 請替換為您的目標資料夾路徑
    target_folder = input("請輸入目標資料夾路徑: ").strip()
    
    # 檢查資料夾是否存在
    if not os.path.isdir(target_folder):
        print(f"錯誤: 資料夾 '{target_folder}' 不存在！")
        sys.exit(1)
    
    # 確認操作
    print(f"即將更改資料夾 '{target_folder}' 及其所有子資料夾中的 .java 檔案為 .txt")
    confirmation = input("確定要繼續嗎？(y/N): ").strip().lower()
    
    if confirmation != 'y' and confirmation != 'yes':
        print("操作已取消。")
        sys.exit(0)
    
    # 執行副檔名更改
    print("開始更改副檔名...")
    changed_files = change_file_extensions(target_folder)
    
    # 顯示結果
    print(f"\n操作完成！共更改了 {len(changed_files)} 個檔案。")
    
    # 可選：顯示更改的檔案列表
    if changed_files:
        show_list = input("是否要顯示更改的檔案列表？(y/N): ").strip().lower()
        if show_list == 'y' or show_list == 'yes':
            print("\n更改的檔案列表:")
            for old_path, new_path in changed_files:
                print(f"  {old_path} -> {new_path}")

if __name__ == "__main__":
    main()