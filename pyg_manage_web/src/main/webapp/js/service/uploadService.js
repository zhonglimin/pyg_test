app.service('uploadService',function ($http) {
    //上传
    this.uploadFile=function () {
        var formData=new FormData();
        formData.append("file",file.files[0]);
        return $http({
            url:'../uploadFile.do',
            method:'POST',
            data:formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        });
    }
})