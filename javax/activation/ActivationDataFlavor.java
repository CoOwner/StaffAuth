package javax.activation;

import java.awt.datatransfer.*;

public class ActivationDataFlavor extends DataFlavor
{
    private String mimeType;
    private MimeType mimeObject;
    private String humanPresentableName;
    private Class representationClass;
    
    public ActivationDataFlavor(final Class representationClass, final String mimeType, final String humanPresentableName) {
        super(mimeType, humanPresentableName);
        this.mimeType = null;
        this.mimeObject = null;
        this.humanPresentableName = null;
        this.representationClass = null;
        this.mimeType = mimeType;
        this.humanPresentableName = humanPresentableName;
        this.representationClass = representationClass;
    }
    
    public ActivationDataFlavor(final Class representationClass, final String humanPresentableName) {
        super(representationClass, humanPresentableName);
        this.mimeType = null;
        this.mimeObject = null;
        this.humanPresentableName = null;
        this.representationClass = null;
        this.mimeType = super.getMimeType();
        this.representationClass = representationClass;
        this.humanPresentableName = humanPresentableName;
    }
    
    public ActivationDataFlavor(final String mimeType, final String humanPresentableName) {
        super(mimeType, humanPresentableName);
        this.mimeType = null;
        this.mimeObject = null;
        this.humanPresentableName = null;
        this.representationClass = null;
        this.mimeType = mimeType;
        try {
            this.representationClass = Class.forName("java.io.InputStream");
        }
        catch (ClassNotFoundException ex) {}
        this.humanPresentableName = humanPresentableName;
    }
    
    @Override
    public String getMimeType() {
        return this.mimeType;
    }
    
    @Override
    public Class getRepresentationClass() {
        return this.representationClass;
    }
    
    @Override
    public String getHumanPresentableName() {
        return this.humanPresentableName;
    }
    
    @Override
    public void setHumanPresentableName(final String humanPresentableName) {
        this.humanPresentableName = humanPresentableName;
    }
    
    @Override
    public boolean equals(final DataFlavor dataFlavor) {
        return this.isMimeTypeEqual(dataFlavor) && dataFlavor.getRepresentationClass() == this.representationClass;
    }
    
    @Override
    public boolean isMimeTypeEqual(final String s) {
        MimeType mimeType;
        try {
            if (this.mimeObject == null) {
                this.mimeObject = new MimeType(this.mimeType);
            }
            mimeType = new MimeType(s);
        }
        catch (MimeTypeParseException ex) {
            return this.mimeType.equalsIgnoreCase(s);
        }
        return this.mimeObject.match(mimeType);
    }
    
    @Override
    @Deprecated
    protected String normalizeMimeTypeParameter(final String s, final String s2) {
        return s2;
    }
    
    @Override
    @Deprecated
    protected String normalizeMimeType(final String s) {
        return s;
    }
}
