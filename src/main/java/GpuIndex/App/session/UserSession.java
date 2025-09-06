package GpuIndex.App.session;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UserSession {
    private List<Map<String, String>> lastSearchResults = new ArrayList<>();
    private String lastQuery = "";

    public List<Map<String, String>> getLastSearchResults() {
        return lastSearchResults;
    }

    public void setLastSearchResults(List<Map<String, String>> lastSearchResults) {
        this.lastSearchResults = lastSearchResults;
    }

    public String getLastQuery() {
        return lastQuery;
    }

    public void setLastQuery(String lastQuery) {
        this.lastQuery = lastQuery;
    }

    public void clear() {
        this.lastSearchResults.clear();
        this.lastQuery = "";
    }

    public boolean hasSearchResults() {
        return !lastSearchResults.isEmpty();
    }

    public int getSearchResultsSize() {
        return lastSearchResults.size();
    }
}
