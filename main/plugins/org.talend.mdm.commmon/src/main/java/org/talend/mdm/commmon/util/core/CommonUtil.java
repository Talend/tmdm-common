package org.talend.mdm.commmon.util.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.TypeMetadata;


public class CommonUtil {

    public static String getErrMsgFromException(Throwable e){
        String msg = e.getLocalizedMessage();
        if (msg != null) {
            Pattern p=Pattern.compile("(.*?):(.*?)"); //$NON-NLS-1$
            Matcher m = p.matcher(msg);
            if (m.matches()) {
                msg = m.group(2);
            }
        }
        return msg;
    }

    public static Object getSuperTypeMaxLength(TypeMetadata originalTypeMetadata, TypeMetadata typeMetadata){
        Object currentLength = typeMetadata.getData(MetadataRepository.DATA_MAX_LENGTH);
        if(currentLength == null){
            for(TypeMetadata type: typeMetadata.getSuperTypes()){
                if(MetadataUtils.getSuperConcreteType(originalTypeMetadata).getName().equals(MetadataUtils.getSuperConcreteType(type).getName())){
                    currentLength = getSuperTypeMaxLength(originalTypeMetadata, type);
                }
            }
        }
        return currentLength;
    }
}
